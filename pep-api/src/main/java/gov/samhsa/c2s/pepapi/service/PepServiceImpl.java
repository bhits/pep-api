package gov.samhsa.c2s.pepapi.service;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import gov.samhsa.c2s.pepapi.infrastructure.PepClient;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessRequestDto;
import gov.samhsa.c2s.pepapi.infrastructure.dto.AccessResponseDto;
import gov.samhsa.c2s.pepapi.service.exception.NoDocumentFoundException;
import gov.samhsa.c2s.pepapi.service.exception.PepClientInterfaceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PepServiceImpl implements PepService {

    private final PepClient pepClient;

    public PepServiceImpl(PepClient pepClient) {
        this.pepClient = pepClient;
    }

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        AccessResponseDto accessResponseDto;
        try {
            accessResponseDto = pepClient.access(accessRequest);

        } catch (HystrixRuntimeException hystrixErr) {
            Throwable causedBy = hystrixErr.getCause();

            if (!(causedBy instanceof FeignException)) {
                log.error("Unexpected instance of HystrixRuntimeException has occurred", hystrixErr);
                throw new PepClientInterfaceException("An unknown error occurred while attempting to communicate with" +
                        " PEP service", hystrixErr);
            }

            int causedByStatus = ((FeignException) causedBy).status();

            switch (causedByStatus) {
                case 404:
                    log.error("PEP client returned a 404 - NO Consent/Document Found REQUEST status" +
                            " to PEP client", causedBy);
                    throw new NoDocumentFoundException("Invalid document was passed to DSS client");
                default:
                    log.error("PEP client returned an unexpected instance of FeignException", causedBy);
                    throw new PepClientInterfaceException("An unknown error occurred while attempting to communicate " +
                            "with" +
                            " PEP service");
            }
        }
        return accessResponseDto;
    }
}
