package gov.samhsa.c2s.pep.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class ControllerConfig {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Document not found")
    public void handleError(Exception e) {
        log.error(e.getMessage(), e);
    }
}
