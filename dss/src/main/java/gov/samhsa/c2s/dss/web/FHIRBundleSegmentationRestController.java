package gov.samhsa.c2s.dss.web;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.c2s.dss.service.FHIRBundleSegmentation;
import gov.samhsa.c2s.dss.service.dto.DSSRequestForFHIR;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFHIR;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class FHIRBundleSegmentationRestController {

    @Autowired
    private FHIRBundleSegmentation fhirBundleSegmentation;

    @RequestMapping(value = "/segmentedFHIRBundle", method = RequestMethod.POST)
    public DSSResponseForFHIR segmentFHIRBundle(@Valid @RequestBody DSSRequestForFHIR request) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException, InvalidOriginalClinicalDocumentException {
        return fhirBundleSegmentation.segmentFHIRBundle(request);
    }
}
