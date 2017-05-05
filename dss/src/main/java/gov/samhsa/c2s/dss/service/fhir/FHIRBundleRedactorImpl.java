package gov.samhsa.c2s.dss.service.fhir;

import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FHIRBundleRedactorImpl implements FHIRBundleRedactor {

    private static final String XPATH_EMBEDDED_FHIR_BUNDLE = "//fhir:EmbeddedFHIRBundle";
    private static final String XPATH_GENERATED_ENTRY_ID = "//fhir:generatedEntryId";

    @Autowired
    private DocumentAccessor documentAccessor;

    @Autowired
    private DocumentXmlConverter documentXmlConverter;

    @Override
    public String cleanUpEmbeddedFHIRBundleFromFactModel(String factModelXml) {
        return cleanUpElements(factModelXml, XPATH_EMBEDDED_FHIR_BUNDLE);
    }

    @Override
    public String cleanUpGeneratedEntryIds(String fhirBundleXml) {
        // Remove all generatedEntryId elements to clean up the FHIR Bundle XML
        return cleanUpElements(fhirBundleXml, XPATH_GENERATED_ENTRY_ID);
    }

    @Override
    public DocumentXmlConverter getDocumentXmlConverter() {
        return this.documentXmlConverter;
    }

    @Override
    public DocumentAccessor getDocumentAccessor() {
        return this.documentAccessor;
    }
}
