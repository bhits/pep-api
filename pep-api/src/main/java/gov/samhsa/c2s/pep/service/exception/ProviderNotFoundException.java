package gov.samhsa.c2s.pep.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ProviderNotFoundException extends RuntimeException {
    public static final String DEFAULT_MESSAGE = "Provider NPI found";

    public ProviderNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public ProviderNotFoundException(String message) {
        super(message);
    }

    public ProviderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProviderNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProviderNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
