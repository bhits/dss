package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.dss.service.dto.DSSRequestForFHIR;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFHIR;

public interface FHIRBundleSegmentation {

    DSSResponseForFHIR segmentFHIRBundle(DSSRequestForFHIR dssRequestForFHIR);
}
