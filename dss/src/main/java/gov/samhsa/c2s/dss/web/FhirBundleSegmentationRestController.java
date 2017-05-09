package gov.samhsa.c2s.dss.web;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.c2s.dss.service.FhirBundleSegmentation;
import gov.samhsa.c2s.dss.service.dto.DSSRequestForFhir;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFhir;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class FhirBundleSegmentationRestController {

    @Autowired
    private FhirBundleSegmentation fhirBundleSegmentation;

    @RequestMapping(value = "/segmentedFhirBundle", method = RequestMethod.POST)
    public DSSResponseForFhir segmentFhirBundle(@Valid @RequestBody DSSRequestForFhir request) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException, InvalidOriginalClinicalDocumentException {
        return fhirBundleSegmentation.segmentFhirBundle(request);
    }
}
