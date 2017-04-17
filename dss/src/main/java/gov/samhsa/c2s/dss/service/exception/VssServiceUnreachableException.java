package gov.samhsa.c2s.dss.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class VssServiceUnreachableException extends RuntimeException {
    public VssServiceUnreachableException(String message) {
        super(message);
    }
}
