package gov.samhsa.c2s.dss.service.fhir;

import gov.samhsa.c2s.dss.service.Redactor;

public interface FHIRBundleRedactor extends Redactor {

    String cleanUpEmbeddedFHIRBundleFromFactModel(String factModelXml);

    String cleanUpGeneratedEntryIds(String fhirBundleXml);
}
