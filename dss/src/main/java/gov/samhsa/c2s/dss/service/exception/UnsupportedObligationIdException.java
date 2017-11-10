package gov.samhsa.c2s.dss.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedObligationIdException extends RuntimeException {
    public UnsupportedObligationIdException() {
    }

    public UnsupportedObligationIdException(String message) {
        super(message);
    }

    public UnsupportedObligationIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedObligationIdException(Throwable cause) {
        super(cause);
    }

    public UnsupportedObligationIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
