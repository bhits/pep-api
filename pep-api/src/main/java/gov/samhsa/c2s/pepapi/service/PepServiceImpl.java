package gov.samhsa.c2s.pepapi.service;

import feign.FeignException;
import gov.samhsa.c2s.pepapi.infrastructure.PepClient;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessRequestDto;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessResponseDto;
import gov.samhsa.c2s.pepapi.service.exception.NoDocumentFoundException;
import gov.samhsa.c2s.pepapi.service.exception.PepClientInterfaceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PepServiceImpl implements PepService {

    private final PepClient pepClient;

    @Autowired
    public PepServiceImpl(PepClient pepClient) {
        this.pepClient = pepClient;
    }

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        log.debug("PEP Service accessDocument Start");
        AccessResponseDto accessResponseDto;
        log.debug("Invoking pep feign client - Start");
        try {
            accessResponseDto = pepClient.access(accessRequest);
            log.debug("Invoking pep feign client - End");
        } catch (FeignException e) {
            int causedByStatus = e.status();
            switch (causedByStatus) {
                case 404:
                    log.error("PEP client returned a 404 - NO Consent/Document Found REQUEST status" +
                            " to PEP client", e);
                    throw new NoDocumentFoundException("Invalid document was passed to DSS client");
                default:
                    log.error("PEP client returned an unexpected instance of FeignException", e);
                    throw new PepClientInterfaceException("An unknown error occurred while attempting to communicate " +
                            "with" +
                            " PEP service");
            }
        } catch (Exception e) {
            log.error("Unexpected instance of HystrixRuntimeException has occurred", e);
            throw new PepClientInterfaceException("An unknown error occurred while attempting to communicate with" +
                    " PEP service", e);
        }
        log.debug("PEP Service accessDocument End");
        return accessResponseDto;
    }
}
