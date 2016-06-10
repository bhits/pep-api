package gov.samhsa.c2s.pep.service;

import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;

public interface PolicyEnforcementPoint {
    AccessResponseDto accessDocument(AccessRequestDto accessRequest);
}
