package gov.samhsa.c2s.dss.service.fhir;

import gov.samhsa.c2s.dss.service.EmbeddedElementExtractor;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;

public interface EmbeddedFHIRBundleExtractor extends EmbeddedElementExtractor {
    /**
     * Extract FHIR Bundle from fact model.
     *
     * @param factModel the fact model
     * @return the string
     * @throws DocumentSegmentationException the document segmentation exception
     */
    String extractFHIRBundleFromFactModel(String factModel) throws DocumentSegmentationException;
}
