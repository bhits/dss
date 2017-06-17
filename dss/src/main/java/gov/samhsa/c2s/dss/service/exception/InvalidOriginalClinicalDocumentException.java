package gov.samhsa.c2s.dss.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOriginalClinicalDocumentException extends
        RuntimeException {

    public InvalidOriginalClinicalDocumentException() {
        super();
    }

    public InvalidOriginalClinicalDocumentException(Exception e) {
        super(e);
    }

    public InvalidOriginalClinicalDocumentException(String message, Exception e) {
        super(message, e);
    }

    public InvalidOriginalClinicalDocumentException(String message) {
        super(message);
    }
}
