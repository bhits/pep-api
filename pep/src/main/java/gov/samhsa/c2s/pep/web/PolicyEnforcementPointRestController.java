package gov.samhsa.c2s.pep.web;

import gov.samhsa.c2s.pep.service.PolicyEnforcementPoint;
import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class PolicyEnforcementPointRestController {
    @Autowired
    private PolicyEnforcementPoint policyEnforcementPoint;

    @RequestMapping(value = "/access", method = RequestMethod.POST)
    public AccessResponseDto access(@Valid @RequestBody AccessRequestDto accessRequest) {
        return policyEnforcementPoint.accessDocument(accessRequest);
    }
}
