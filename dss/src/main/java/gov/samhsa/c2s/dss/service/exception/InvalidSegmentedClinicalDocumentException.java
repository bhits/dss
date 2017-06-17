package gov.samhsa.c2s.dss.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSegmentedClinicalDocumentException extends
        RuntimeException {

    public InvalidSegmentedClinicalDocumentException() {
        super();
    }

    public InvalidSegmentedClinicalDocumentException(Exception e) {
        super(e);
    }

    public InvalidSegmentedClinicalDocumentException(String message, Exception e) {
        super(message, e);
    }

    public InvalidSegmentedClinicalDocumentException(String message) {
        super(message);
    }
}
