package gov.samhsa.c2s.pep.infrastructure;

import gov.samhsa.c2s.pep.infrastructure.dto.DSSRequest;
import gov.samhsa.c2s.pep.infrastructure.dto.DSSResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@FeignClient("dss")
public interface DssService {

    @RequestMapping(value = "/segmentedDocument", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    DSSResponse segmentDocument(@Valid @RequestBody DSSRequest request);
}
