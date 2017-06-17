package gov.samhsa.c2s.dss.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AdditionalMetadataGeneratorException extends RuntimeException {

    /**
     * Instantiates a new d s4 p exception.
     */
    public AdditionalMetadataGeneratorException() {
        super();
    }

    /**
     * Instantiates a new d s4 p exception.
     *
     * @param arg0 the arg0
     * @param arg1 the arg1
     */
    public AdditionalMetadataGeneratorException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Instantiates a new d s4 p exception.
     *
     * @param arg0 the arg0
     */
    public AdditionalMetadataGeneratorException(String arg0) {
        super(arg0);
    }

    /**
     * Instantiates a new d s4 p exception.
     *
     * @param arg0 the arg0
     */
    public AdditionalMetadataGeneratorException(Throwable arg0) {
        super(arg0);
    }
}
