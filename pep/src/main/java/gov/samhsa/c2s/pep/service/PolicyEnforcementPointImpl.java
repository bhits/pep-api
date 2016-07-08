package gov.samhsa.c2s.pep.service;

import com.netflix.discovery.converters.Auto;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import gov.samhsa.c2s.pep.infrastructure.ContextHandlerService;
import gov.samhsa.c2s.pep.infrastructure.DssService;
import gov.samhsa.c2s.pep.infrastructure.ProviderNpiLookupServiceImpl;
import gov.samhsa.c2s.pep.infrastructure.dto.*;
import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import gov.samhsa.c2s.pep.service.dto.DocumentRequestDto;
import gov.samhsa.c2s.pep.service.dto.DocumentsResponseDto;
import gov.samhsa.c2s.pep.service.exception.DocumentNotFoundException;
import gov.samhsa.c2s.pep.service.exception.InternalServerErrorException;
import gov.samhsa.c2s.pep.service.exception.ProviderNotFoundException;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorException;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverterImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Service
@Slf4j
public class PolicyEnforcementPointImpl implements PolicyEnforcementPoint {

    private static final String PERMIT = "permit";

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

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        val xacmlRequest = accessRequest.getXacmlRequest();
        val xacmlResponse = enforcePolicy(xacmlRequest);
        val xacmlResult = XacmlResult.from(xacmlRequest, xacmlResponse);

        assertPDPPermitDecision(xacmlResponse);

        val dssRequest = accessRequest.toDSSRequest(xacmlResult);
        val dssResponse = dssService.segmentDocument(dssRequest);
        val accessResponse = AccessResponseDto.from(dssResponse);
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
    public DocumentsResponseDto getCCDDocuments(String username, DocumentRequestDto documentRequestDto) {
        final String recipientNpi = Optional.ofNullable(username)
                                            .map(providerNpiLookupService.getUsers()::get)
                                            .orElseThrow(ProviderNotFoundException::new);

        String parameters = composeIExHubParameter(documentRequestDto.getMrn(), documentRequestDto.getDomain());
        HttpHeaders headers = new HttpHeaders();
        headers.set(iexhubParameterKey,parameters );
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        // Calling IExHub to get ccd document
        ResponseEntity<DocumentsResponseDto> response  = restTemplate.exchange(iexhubUrl, HttpMethod.GET, entity, DocumentsResponseDto.class);

        if(!response.getStatusCode().equals(HttpStatus.OK)){
            throw new DocumentNotFoundException("Cannot get CCD document from IExhub.");
        }

        Optional<String> intermediateNPI = Optional.empty();
        DocumentsResponseDto patientDataResponse = response.getBody();

        if(patientDataResponse.getDocuments().size() > 0){
            String documentStr = patientDataResponse.getDocuments().get(0);
            intermediateNPI = getIntermediateNPI(recipientNpi, documentStr);
            XacmlRequestDto xacmlRequestDto = createXacmlRequestDto(recipientNpi, intermediateNPI.get(), documentRequestDto);
            XacmlResponseDto xacmlResponseDto = contextHandler.enforcePolicy(xacmlRequestDto);

            assertPDPPermitDecision(xacmlResponseDto);

            XacmlResult xacmlResult = XacmlResult.from(xacmlRequestDto, xacmlResponseDto);

            DSSRequest dssRequest = new DSSRequest();
            dssRequest.setDocument(documentStr.getBytes(StandardCharsets.UTF_8));
            dssRequest.setXacmlResult(xacmlResult);
            val dssResponse = dssService.segmentDocument(dssRequest);
            System.out.println(dssResponse);
        }else{
            return new DocumentsResponseDto();
        }

        return null;
    }

    private XacmlRequestDto createXacmlRequestDto(String recipientNpi, String intermediateNPI, DocumentRequestDto documentRequestDto){
        XacmlRequestDto xacmlRequestDto = new XacmlRequestDto();
        xacmlRequestDto.setIntermediaryNpi(intermediateNPI);

        PatientIdDto patientIdDto =  new PatientIdDto();
        patientIdDto.setExtension(documentRequestDto.getMrn());
        patientIdDto.setRoot(documentRequestDto.getDomain());

        xacmlRequestDto.setPatientId(patientIdDto);
        xacmlRequestDto.setPurposeOfUse(documentRequestDto.getPurposeOfUse());
        xacmlRequestDto.setRecipientNpi(recipientNpi);

        return xacmlRequestDto;
    }
    private String composeIExHubParameter(String mrn, String domain){
        if(mrn!=null && domain!=null && iexhubParameterSuffix != null){
            String patientId = mrn + "^^^&" + domain + "&ISO";
            return  iexhubParameterSuffix.replace("PATIENT_ID_VALUE", "'" + patientId + "'");
        }
        return null;
    }

    private String getRootFromMRN(String mrn){
        return mrn.split("&")[1];
    }

    private String getExtensionFromMRN(String mrn){
        return mrn.split("&")[1];
    }

    private Optional<String> getIntermediateNPI(String recipientNpi, String documentStr) {
        String docStr = documentStr.replaceAll("\n|\r", "");
        Document ccdDocument = documentXmlConverter.loadDocument(docStr);

        try {
            Optional<Node> optionalNode = documentAccessor.getNode(ccdDocument, "/hl7:ClinicalDocument/hl7:author[1]/hl7:assignedAuthor[1]/hl7:id[@root='2.16.840.1.113883.4.6'][1]/@extension");
            if(optionalNode.isPresent()){
                Node node = optionalNode.get();
                String value = node.getNodeValue();
                return optionalNode.map(Node::getNodeValue);
            }else{
                log.error("Cannot find provider NPI in document.");
            }

        }catch (DocumentAccessorException e){
                log.error(e.getStackTrace().toString());
        }
        return Optional.empty();
    }

}
