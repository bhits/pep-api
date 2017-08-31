package gov.samhsa.c2s.pepapi.service;


import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessRequestDto;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessResponseDto;

public interface PepService {
    AccessResponseDto accessDocument(AccessRequestDto accessRequest);
}
