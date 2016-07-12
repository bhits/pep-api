package gov.samhsa.c2s.pep.service;

import gov.samhsa.c2s.pep.service.dto.*;

public interface PolicyEnforcementPoint {
    AccessResponseDto accessDocument(AccessRequestDto accessRequest);
    SegmentedDocumentsResponseDto getCCDDocuments(String username, DocumentRequestDto documentRequestDto);

}
