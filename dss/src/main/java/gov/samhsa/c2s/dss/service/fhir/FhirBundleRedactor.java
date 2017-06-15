package gov.samhsa.c2s.dss.service.fhir;

import gov.samhsa.c2s.dss.service.Redactor;

public interface FhirBundleRedactor extends Redactor {

    String cleanUpEmbeddedFhirBundleFromFactModel(String factModelXml);

    String cleanUpGeneratedEntryIds(String fhirBundleXml);
}
