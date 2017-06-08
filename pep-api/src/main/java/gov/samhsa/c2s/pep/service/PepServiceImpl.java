package gov.samhsa.c2s.pep.service;

import gov.samhsa.c2s.pep.infrastructure.PepClient;
import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
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
