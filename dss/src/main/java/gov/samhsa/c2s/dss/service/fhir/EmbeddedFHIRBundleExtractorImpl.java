package gov.samhsa.c2s.dss.service.fhir;

import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmbeddedFHIRBundleExtractorImpl implements EmbeddedFHIRBundleExtractor {

    private static final String XPATH_FHIR_BUNDLE = "//fhir:Bundle";

    @Autowired
    private DocumentXmlConverter documentXmlConverter;

    @Autowired
    private DocumentAccessor documentAccessor;

    @Override
    public String extractFHIRBundleFromFactModel(String factModel) throws DocumentSegmentationException {
        return extractEmbeddedElement(factModel, XPATH_FHIR_BUNDLE);
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
