package gov.samhsa.c2s.dss.service;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.c2s.dss.service.dto.DSSRequest;
import gov.samhsa.c2s.dss.service.dto.DSSResponse;
import gov.samhsa.c2s.dss.service.dto.SegmentDocumentResponse;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;

import java.io.IOException;

/**
 * The Interface DocumentSegmentation.
 */
public interface DocumentSegmentation {

    /**
     * Segment document.
     *
     * @param dssRequest the document
     * @return the segment document response
     * @throws XmlDocumentReadFailureException           the xml document read failure exception
     * @throws InvalidSegmentedClinicalDocumentException the invalid segmented clinical document exception
     * @throws AuditException                            the audit exception
     */
    DSSResponse segmentDocument(DSSRequest dssRequest);

    /**
     * Sets the additional metadata for segmented clinical document.
     *
     * @param segmentDocumentResponse  the segment document response
     * @param senderEmailAddress       the sender email address
     * @param recipientEmailAddress    the recipient email address
     * @param xdsDocumentEntryUniqueId the xds document entry unique id
     * @param xacmlResult              the xacml result
     */
    void setAdditionalMetadataForSegmentedClinicalDocument(
            SegmentDocumentResponse segmentDocumentResponse,
            String senderEmailAddress, String recipientEmailAddress,
            String xdsDocumentEntryUniqueId, XacmlResult xacmlResult);

    /**
     * Sets the document payload raw data.
     *
     * @param segmentDocumentResponse the segment document response
     * @param packageAsXdm            the package as xdm
     * @param senderEmailAddress      the sender email address
     * @param recipientEmailAddress   the recipient email address
     * @param xacmlResult             the xacml result
     * @throws Exception   the exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void setDocumentPayloadRawData(
            SegmentDocumentResponse segmentDocumentResponse,
            boolean packageAsXdm, String senderEmailAddress,
            String recipientEmailAddress, XacmlResult xacmlResult)
            throws Exception;
}
