package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.dss.service.dto.DSSRequestForFhir;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFhir;
import org.hl7.fhir.dstu3.model.Bundle;

public interface FhirBundleSegmentation {

    DSSResponseForFhir segmentFhirBundle(DSSRequestForFhir dssRequestForFhir);

    Bundle redactFhirBundle(Bundle fhirbundle,XacmlResult xacmlResult);

    DSSResponseForFhir redactAndUpdateFhirBundle(DSSRequestForFhir dssRequestForFhir);

}
