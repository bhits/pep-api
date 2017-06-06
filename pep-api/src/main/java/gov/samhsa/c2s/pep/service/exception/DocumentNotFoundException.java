package gov.samhsa.c2s.pep.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class DocumentNotFoundException extends RuntimeException {
    public static final String DEFAULT_MESSAGE = "Document not found";

    public DocumentNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentNotFoundException(Throwable cause) {
        super(cause);
    }

    public DocumentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
