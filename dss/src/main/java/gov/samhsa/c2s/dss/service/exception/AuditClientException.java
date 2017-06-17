package gov.samhsa.c2s.dss.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AuditClientException extends RuntimeException {

    /**
     * Instantiates a new AuditClient exception.
     */
    public AuditClientException() {
        super();
    }

    /**
     * Instantiates a new AuditClient exception.
     *
     * @param arg0 the arg0
     * @param arg1 the arg1
     */
    public AuditClientException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Instantiates a new AuditClient exception.
     *
     * @param arg0 the arg0
     */
    public AuditClientException(String arg0) {
        super(arg0);
    }

    /**
     * Instantiates a new AuditClient exception.
     *
     * @param arg0 the arg0
     */
    public AuditClientException(Throwable arg0) {
        super(arg0);
    }
}
