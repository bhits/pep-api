package gov.samhsa.c2s.pep.service;

import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import gov.samhsa.c2s.pep.service.dto.SegmentedDocumentsResponseDto;

public interface PolicyEnforcementPoint {
    AccessResponseDto accessDocument(AccessRequestDto accessRequest);

    SegmentedDocumentsResponseDto getCCDDocuments(String username, String mrn, String purposeOfUse, String domain);
}
