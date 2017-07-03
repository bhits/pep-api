package gov.samhsa.c2s.pepapi.web;

import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessRequestDto;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessResponseDto;
import gov.samhsa.c2s.pepapi.service.PepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("pep")
public class PepRestController {

    @Autowired
    private PepService pepService;

    @RequestMapping(value = "/access", method = RequestMethod.POST)
    public AccessResponseDto access(@Valid @RequestBody AccessRequestDto accessRequest) {
        return pepService.accessDocument(accessRequest);
    }
}
