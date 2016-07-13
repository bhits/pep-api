package gov.samhsa.c2s.pep.web;

import gov.samhsa.c2s.pep.service.PolicyEnforcementPoint;
import gov.samhsa.c2s.pep.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
public class PolicyEnforcementPointRestController {
    @Autowired
    private PolicyEnforcementPoint policyEnforcementPoint;

    @RequestMapping(value = "/access", method = RequestMethod.POST)
    public AccessResponseDto access(@Valid @RequestBody AccessRequestDto accessRequest) {
        return policyEnforcementPoint.accessDocument(accessRequest);
    }

    @RequestMapping(value = "/documents", method = RequestMethod.GET)
    public SegmentedDocumentsResponseDto getDocuments(@RequestParam("mrn") String mrn, @RequestParam("purposeOfUse") String purposeOfUse, @RequestParam("domain") String domain, Principal principal) {
        return policyEnforcementPoint.getCCDDocuments(principal.getName(), mrn, purposeOfUse, domain);
    }
}
