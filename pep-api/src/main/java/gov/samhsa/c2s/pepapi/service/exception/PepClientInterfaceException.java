package gov.samhsa.c2s.pepapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class PepClientInterfaceException extends RuntimeException {
    public static final String DEFAULT_MESSAGE = "Document not found";

    public PepClientInterfaceException() {
        super(DEFAULT_MESSAGE);
    }

    public PepClientInterfaceException(String message) {
        super(message);
    }

    public PepClientInterfaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PepClientInterfaceException(Throwable cause) {
        super(cause);
    }

    public PepClientInterfaceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
