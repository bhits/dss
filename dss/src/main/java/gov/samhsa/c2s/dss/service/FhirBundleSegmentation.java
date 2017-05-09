package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.dss.service.dto.DSSRequestForFhir;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFhir;

public interface FhirBundleSegmentation {

    DSSResponseForFhir segmentFhirBundle(DSSRequestForFhir dssRequestForFhir);
}
