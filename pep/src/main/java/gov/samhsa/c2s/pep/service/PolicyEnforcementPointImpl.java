package gov.samhsa.c2s.pep.service;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import gov.samhsa.c2s.pep.infrastructure.ContextHandlerService;
import gov.samhsa.c2s.pep.infrastructure.DssService;
import gov.samhsa.c2s.pep.infrastructure.ProviderNpiLookupServiceImpl;
import gov.samhsa.c2s.pep.infrastructure.dto.*;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResponseDto;
import gov.samhsa.c2s.pep.service.dto.*;
import gov.samhsa.c2s.pep.service.exception.DocumentNotFoundException;
import gov.samhsa.c2s.pep.service.exception.InternalServerErrorException;
import gov.samhsa.c2s.pep.service.exception.ProviderNotFoundException;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorException;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.document.transformer.XmlTransformer;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.param.Params;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.URIResolver;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class PolicyEnforcementPointImpl implements PolicyEnforcementPoint {

    private static final String PERMIT = "permit";
    private static final String CCD_XSL_PATH = "CCDA.xsl";

    private final Logger log = LoggerFactory.getLogger(this);

    @Autowired
    private ContextHandlerService contextHandler;

    @Autowired
    private DssService dssService;

    @Value("${pep.iexhub.param-key}")
    private String iexhubParameterKey;

    @Value("${pep.iexhub.param-value-suffix}")
    private String iexhubParameterSuffix;

    @Value("${pep.iexhub.url}")
    private String iexhubUrl;

    @Value("${pep.patient.document-name}")
    private String patientDocumentName;

    @Autowired
    private RestTemplate restTemplate;
    /**
     * The document xml converter.
     */
    @Autowired
    private DocumentXmlConverter documentXmlConverter;

    /**
     * The document accessor.
     */
    @Autowired
    private DocumentAccessor documentAccessor;

    @Autowired
    private ProviderNpiLookupServiceImpl providerNpiLookupService;

    @Autowired
    private XmlTransformer xmlTransformer;

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        log.info("Initiating PolicyEnforcementPoint.accessDocument flow");
        log.debug(accessRequest::toString);
        val xacmlRequest = accessRequest.getXacmlRequest();
        log.debug(xacmlRequest::toString);
        val xacmlResponse = enforcePolicy(xacmlRequest);
        log.debug(xacmlResponse::toString);
        val xacmlResult = XacmlResult.from(xacmlRequest, xacmlResponse);
        log.debug(xacmlResult::toString);

        assertPDPPermitDecision(xacmlResponse);

        val dssRequest = accessRequest.toDSSRequest(xacmlResult);
        log.debug(dssRequest::toString);
        val dssResponse = dssService.segmentDocument(dssRequest);
        log.debug(dssResponse::toString);
        val accessResponse = AccessResponseDto.from(dssResponse);
        log.debug(accessResponse::toString);
        log.info("Completed PolicyEnforcementPoint.accessDocument flow, returning response");
        return accessResponse;
    }

    private void assertPDPPermitDecision(XacmlResponseDto xacmlResponse) {
        Optional.of(xacmlResponse)
                .map(XacmlResponseDto::getPdpDecision)
                .filter(PERMIT::equalsIgnoreCase)
                .orElseThrow(DocumentNotFoundException::new);
    }

    private XacmlResponseDto enforcePolicy(XacmlRequestDto xacmlRequest) {
        try {
            return contextHandler.enforcePolicy(xacmlRequest);
        } catch (HystrixRuntimeException e) {
            final FeignException feignException = Optional.of(e)
                    .map(HystrixRuntimeException::getCause)
                    .filter(FeignException.class::isInstance)
                    .map(FeignException.class::cast)
                    .orElseThrow(() -> {
                        log.error(e.getMessage(), e);
                        return new InternalServerErrorException(e);
                    });
            if (HttpStatus.NOT_FOUND.equals(getHttpStatus(feignException))) {
                log.info("consent not found");
                log.debug(e.getMessage(), e);
                throw new DocumentNotFoundException();
            } else {
                log.error(e.getMessage(), e);
                throw new InternalServerErrorException(e);
            }
        }
    }

    private HttpStatus getHttpStatus(FeignException e) {
        return HttpStatus.valueOf(e.status());
    }

    @Override
    public SegmentedDocumentsResponseDto getCCDDocuments(String username, String mrn, String purposeOfUse, String domain) {
        SegmentedDocumentsResponseDto segmentedDocumentsResponseDto = new SegmentedDocumentsResponseDto();
        try {

            final String recipientNpi = Optional.ofNullable(username)
                    .map(providerNpiLookupService.getUsers()::get)
                    .orElseThrow(ProviderNotFoundException::new);

            String parameters = composeIExHubParameter(mrn, domain);
            HttpHeaders headers = new HttpHeaders();
            headers.set(iexhubParameterKey, parameters);
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

            // Calling IExHub to get ccd document
            ResponseEntity<DocumentsResponseDto> response = restTemplate.exchange(iexhubUrl, HttpMethod.GET, entity, DocumentsResponseDto.class);

            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                throw new DocumentNotFoundException("Cannot get CCD document from IExhub.");
            }

            Optional<String> intermediateNPI = Optional.empty();
            DocumentsResponseDto documentsResponseDto = response.getBody();

            // Filtering for Sally Share documents
            List<PatientDocument> collect = documentsResponseDto.getDocuments().stream().filter(doc -> patientDocumentName.equals(doc.getName())).collect(toList());
            documentsResponseDto.setDocuments(collect);

            for (PatientDocument patientDocument : documentsResponseDto.getDocuments()) {
                String documentStr = patientDocument.getDocument();
                intermediateNPI = getIntermediateNPI(documentStr);
                XacmlRequestDto xacmlRequestDto = createXacmlRequestDto(recipientNpi, intermediateNPI.get(), mrn, purposeOfUse, domain);
                XacmlResponseDto xacmlResponseDto = contextHandler.enforcePolicy(xacmlRequestDto);

                if (xacmlResponseDto.getPdpDecision().equalsIgnoreCase(PERMIT)) {
                    XacmlResult xacmlResult = XacmlResult.from(xacmlRequestDto, xacmlResponseDto);

                    DSSRequest dssRequest = new DSSRequest();
                    dssRequest.setDocument(documentStr.getBytes(StandardCharsets.UTF_8));
                    dssRequest.setXacmlResult(xacmlResult);
                    DSSResponse dssResponse = dssService.segmentDocument(dssRequest);

                    String segmentedDocumentStr = new String(dssResponse.getSegmentedDocument());
                    final Document segmentedClinicalDocumentDoc = documentXmlConverter.loadDocument(segmentedDocumentStr);
                    // xslt transformation
                    final String xslUrl = Thread.currentThread().getContextClassLoader().getResource(CCD_XSL_PATH).toString();

                    final String output = xmlTransformer.transform(segmentedClinicalDocumentDoc, xslUrl, Optional.<Params>empty(), Optional.<URIResolver>empty());
                    patientDocument.setDocument(output);

                    segmentedDocumentsResponseDto = toSegmentedDocumentResponse(documentsResponseDto);
                } else {
                    log.info("PDP DENY decision for: " + patientDocument.getName());
                }
            }

        } catch (Exception e) {
            log.error(e.getStackTrace().toString());
        } finally {
            return segmentedDocumentsResponseDto;
        }
    }

    private SegmentedDocumentsResponseDto toSegmentedDocumentResponse(DocumentsResponseDto documentsResponseDto) {
        ArrayList<SegmentedPatientDocument> segmentedPatientDocuments = new ArrayList<SegmentedPatientDocument>();

        for (PatientDocument p : documentsResponseDto.getDocuments()) {
            SegmentedPatientDocument segmentedPatientDocument = new SegmentedPatientDocument(p.getName(), p.getDocument().getBytes(StandardCharsets.UTF_8));
            segmentedPatientDocuments.add(segmentedPatientDocument);
        }

        SegmentedDocumentsResponseDto segmentedDocumentsResponseDto = new SegmentedDocumentsResponseDto();
        segmentedDocumentsResponseDto.setDocuments(segmentedPatientDocuments);
        return segmentedDocumentsResponseDto;
    }

    private XacmlRequestDto createXacmlRequestDto(String recipientNpi, String intermediateNPI, String mrn, String purpose, String domain) {
        XacmlRequestDto xacmlRequestDto = new XacmlRequestDto();
        xacmlRequestDto.setIntermediaryNpi(intermediateNPI);

        PatientIdDto patientIdDto = new PatientIdDto();
        patientIdDto.setExtension(mrn);
        patientIdDto.setRoot(domain);

        xacmlRequestDto.setPatientId(patientIdDto);

        SubjectPurposeOfUse purposeOfUse = SubjectPurposeOfUse.valueOf(purpose);
        xacmlRequestDto.setPurposeOfUse(purposeOfUse);

        xacmlRequestDto.setRecipientNpi(recipientNpi);

        return xacmlRequestDto;
    }

    private String composeIExHubParameter(String mrn, String domain) {
        if (mrn != null && domain != null && iexhubParameterSuffix != null) {
            String patientId = mrn + "^^^&" + domain + "&ISO";
            return iexhubParameterSuffix.replace("PATIENT_ID_VALUE", "'" + patientId + "'");
        }
        return null;
    }

    private String getRootFromMRN(String mrn) {
        return mrn.split("&")[1];
    }

    private String getExtensionFromMRN(String mrn) {
        return mrn.split("&")[1];
    }

    private Optional<String> getIntermediateNPI(String documentStr) {
        Document ccdDocument = documentXmlConverter.loadDocument(documentStr);
        try {
            Optional<Node> optionalNode = documentAccessor.getNode(ccdDocument, "/hl7:ClinicalDocument/hl7:author[1]/hl7:assignedAuthor[1]/hl7:id[@root='2.16.840.1.113883.4.6'][1]/@extension");
            if (optionalNode.isPresent()) {
                Node node = optionalNode.get();
                String value = node.getNodeValue();
                return optionalNode.map(Node::getNodeValue);
            } else {
                log.error("Cannot find provider NPI in document.");
            }

        } catch (DocumentAccessorException e) {
            log.error(e.getStackTrace().toString());
        }
        return Optional.empty();
    }

}
