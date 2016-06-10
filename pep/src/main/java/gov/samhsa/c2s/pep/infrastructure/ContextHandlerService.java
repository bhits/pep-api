package gov.samhsa.c2s.pep.infrastructure;

import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResponseDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@FeignClient("context-handler")
public interface ContextHandlerService {

    @RequestMapping(value = "/policyEnforcement", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    XacmlResponseDto enforcePolicy(@Valid @RequestBody XacmlRequestDto xacmlRequest);

}
