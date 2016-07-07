package gov.samhsa.c2s.pep.service;

import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import gov.samhsa.c2s.pep.service.dto.DocumentRequestDto;
import gov.samhsa.c2s.pep.service.dto.DocumentsResponseDto;

public interface PolicyEnforcementPoint {
    AccessResponseDto accessDocument(AccessRequestDto accessRequest);
    DocumentsResponseDto getCCDDocuments(String username, DocumentRequestDto documentRequestDto);

}
