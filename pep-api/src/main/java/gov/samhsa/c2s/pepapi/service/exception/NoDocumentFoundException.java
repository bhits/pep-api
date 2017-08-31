package gov.samhsa.c2s.pepapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoDocumentFoundException extends RuntimeException {
    public static final String DEFAULT_MESSAGE = "Document not found";

    public NoDocumentFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public NoDocumentFoundException(String message) {
        super(message);
    }

    public NoDocumentFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoDocumentFoundException(Throwable cause) {
        super(cause);
    }

    public NoDocumentFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
