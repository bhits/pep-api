package gov.samhsa.c2s.pepapi.service;

import gov.samhsa.c2s.pepapi.infrastructure.PepClient;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessRequestDto;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessResponseDto;
import org.springframework.stereotype.Service;

@Service
public class PepServiceImpl implements PepService {

    private final PepClient pepClient;

    public PepServiceImpl(PepClient pepClient) {
        this.pepClient = pepClient;
    }

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        return pepClient.access(accessRequest);
    }
}
