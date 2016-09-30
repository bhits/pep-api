package gov.samhsa.c2s.pep.service;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.pep.infrastructure.ContextHandlerService;
import gov.samhsa.c2s.pep.infrastructure.DssService;
import gov.samhsa.c2s.pep.infrastructure.ProviderNpiLookupService;
import gov.samhsa.c2s.pep.infrastructure.dto.*;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResponseDto;
import gov.samhsa.c2s.pep.service.dto.*;
import gov.samhsa.c2s.pep.service.exception.DocumentNotFoundException;
import gov.samhsa.c2s.pep.service.exception.InternalServerErrorException;
import gov.samhsa.c2s.pep.service.exception.ProviderNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class PolicyEnforcementPointImpl implements PolicyEnforcementPoint {

    private static final String PERMIT = "permit";
    private static final String CCD_XSL_PATH = "CDA.xsl";

    private final Logger logger = LoggerFactory.getLogger(this);

    @Value("${pep.iexhub.param-key}")
    private String iexhubParameterKey;

    @Value("${pep.iexhub.param-value-suffix}")
    private String iexhubParameterSuffix;

    @Value("${pep.iexhub.url}")
    private String iexhubUrl;

    @Value("${pep.author-npi-xpath}")
    private String authorNpiXPath;

    @Value("${pep.patient.document-name}")
    private String patientDocumentName;

    @Autowired
    private ContextHandlerService contextHandler;

    @Autowired
    private DssService dssService;

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
    private ProviderNpiLookupService providerNpiLookupService;

    @Autowired
    private XmlTransformer xmlTransformer;

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        logger.info("Initiating PolicyEnforcementPoint.accessDocument flow");
        logger.debug(accessRequest::toString);
        final XacmlRequestDto xacmlRequest = accessRequest.getXacmlRequest();
        logger.debug(xacmlRequest::toString);
        final XacmlResponseDto xacmlResponse = enforcePolicy(xacmlRequest);
        logger.debug(xacmlResponse::toString);
        final XacmlResult xacmlResult = XacmlResult.from(xacmlRequest, xacmlResponse);
        logger.debug(xacmlResult::toString);

        assertPDPPermitDecision(xacmlResponse);

        final DSSRequest dssRequest = accessRequest.toDSSRequest(xacmlResult);
        logger.debug(dssRequest::toString);
        final DSSResponse dssResponse = dssService.segmentDocument(dssRequest);
        logger.debug(dssResponse::toString);
        final AccessResponseDto accessResponse = AccessResponseDto.from(dssResponse);
        logger.debug(accessResponse::toString);
        logger.info("Completed PolicyEnforcementPoint.accessDocument flow, returning response");
        return accessResponse;
    }

    @Override
    public SegmentedDocumentsResponseDto getCCDDocuments(String username, String mrn, String purposeOfUse, String domain) {
        SegmentedDocumentsResponseDto segmentedDocumentsResponseDto = new SegmentedDocumentsResponseDto();
        try {

            final String recipientNpi = Optional.ofNullable(username)
                    .map(providerNpiLookupService.getUsers()::get)
                    .orElseThrow(ProviderNotFoundException::new);

            final String parameters = composeIExHubParameter(mrn, domain);
            final HttpHeaders headers = new HttpHeaders();
            headers.set(iexhubParameterKey, parameters);
            final HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            // Calling IExHub to get ccd document
            ResponseEntity<DocumentsResponseDto> response = restTemplate.exchange(iexhubUrl, HttpMethod.GET, entity, DocumentsResponseDto.class);

            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                throw new DocumentNotFoundException("Cannot get CCD document from IExhub.");
            }

            final DocumentsResponseDto documentsResponseDto = response.getBody();
            logger.debug(documentsResponseDto::toString);

            // Filtering for Sally Share documents
            final List<PatientDocument> collect = documentsResponseDto.getDocuments().stream().filter(doc -> patientDocumentName.equals(doc.getName())).collect(toList());
            documentsResponseDto.setDocuments(collect);

            for (PatientDocument patientDocument : documentsResponseDto.getDocuments()) {
                final String documentStr = patientDocument.getDocument();
                final Optional<String> intermediateNPI = getIntermediateNPI(documentStr);
                final XacmlRequestDto xacmlRequestDto = createXacmlRequestDto(recipientNpi, intermediateNPI.get(), mrn, purposeOfUse, domain);
                logger.debug(xacmlRequestDto::toString);
                final XacmlResponseDto xacmlResponseDto = contextHandler.enforcePolicy(xacmlRequestDto);
                logger.debug(xacmlResponseDto::toString);

                if (xacmlResponseDto.getPdpDecision().equalsIgnoreCase(PERMIT)) {
                    final XacmlResult xacmlResult = XacmlResult.from(xacmlRequestDto, xacmlResponseDto);

                    final DSSRequest dssRequest = new DSSRequest();
                    dssRequest.setDocument(documentStr.getBytes(StandardCharsets.UTF_8));
                    dssRequest.setXacmlResult(xacmlResult);
                    logger.debug(dssRequest::toString);
                    DSSResponse dssResponse = dssService.segmentDocument(dssRequest);
                    logger.debug(dssResponse::toString);

                    final String segmentedDocumentStr = new String(dssResponse.getSegmentedDocument());
                    logger.debug(() -> "segmented: " + segmentedDocumentStr);
                    final Document segmentedClinicalDocumentDoc = documentXmlConverter.loadDocument(segmentedDocumentStr);
                    // xslt transformation
                    final String xslUrl = Thread.currentThread().getContextClassLoader().getResource(CCD_XSL_PATH).toString();

                    final String output = xmlTransformer.transform(segmentedClinicalDocumentDoc, xslUrl, Optional.empty(), Optional.empty());
                    patientDocument.setDocument(output);
                    logger.debug(() -> "transformed: " + output);

                    segmentedDocumentsResponseDto = toSegmentedDocumentResponse(documentsResponseDto);
                } else {
                    logger.info("PDP DENY decision for: " + patientDocument.getName());
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            return segmentedDocumentsResponseDto;
        }
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
                        logger.error(e.getMessage(), e);
                        return new InternalServerErrorException(e);
                    });
            if (HttpStatus.NOT_FOUND.equals(getHttpStatus(feignException))) {
                logger.info("consent not found");
                logger.debug(e.getMessage(), e);
                throw new DocumentNotFoundException();
            } else {
                logger.error(e.getMessage(), e);
                throw new InternalServerErrorException(e);
            }
        }
    }

    private HttpStatus getHttpStatus(FeignException e) {
        return HttpStatus.valueOf(e.status());
    }

    private SegmentedDocumentsResponseDto toSegmentedDocumentResponse(DocumentsResponseDto documentsResponseDto) {
        List<SegmentedPatientDocument> segmentedPatientDocuments = new ArrayList<>();

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

    private Optional<String> getIntermediateNPI(String documentStr) {
        return Optional.ofNullable(documentXmlConverter.loadDocument(documentStr))
                .flatMap(this::getAuthorNpi)
                .map(Node::getNodeValue);
    }

    private Optional<Node> getAuthorNpi(Document document) {
        try {
            return documentAccessor.getNode(document, authorNpiXPath);
        } catch (DocumentAccessorException e) {
            logger.error(e.getMessage());
            logger.debug(e.getMessage(), e);
            return Optional.empty();
        }
    }
}
