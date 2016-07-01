package gov.samhsa.c2s.pep.service;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import gov.samhsa.c2s.pep.infrastructure.ContextHandlerService;
import gov.samhsa.c2s.pep.infrastructure.DssService;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResponseDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResult;
import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import gov.samhsa.c2s.pep.service.dto.DocumentRequestDto;
import gov.samhsa.c2s.pep.service.dto.DocumentsResponseDto;
import gov.samhsa.c2s.pep.service.exception.DocumentNotFoundException;
import gov.samhsa.c2s.pep.service.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;

import java.net.URI;
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
    public DocumentsResponseDto getCCDDocuments(DocumentRequestDto documentRequestDto) {
        String parameter = composeIExHubParameter(documentRequestDto.getMrn());
        HttpHeaders headers = new HttpHeaders();
        headers.set(iexhubParameterKey,parameter );
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<DocumentsResponseDto> response  = restTemplate.exchange(iexhubUrl, HttpMethod.GET, entity, DocumentsResponseDto.class);

        if(response.getStatusCode().equals(HttpStatus.OK)){
            DocumentsResponseDto patientDataResponse = response.getBody();
            System.out.println(patientDataResponse.getDocuments());
        }

        return null;
    }

    private String composeIExHubParameter(String parameter){
        if(parameter!=null && iexhubParameterSuffix != null){
            return  iexhubParameterSuffix.replace("MRN", "'" + parameter + "'");
        }
        return null;
    }
}
