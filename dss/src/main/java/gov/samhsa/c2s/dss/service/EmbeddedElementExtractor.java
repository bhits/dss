package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterException;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public interface EmbeddedElementExtractor extends CoreDocumentUtilitiesAware {
    /**
     * Extracts an embedded element from a {@code sourceXml} and returns as a separate standalone XML
     *
     * @return extracted embedded xml
     */
    default String extractEmbeddedElement(String sourceXml, String xPathExprEmbeddedElement) {
        try {
            final DocumentXmlConverter documentXmlConverter = getDocumentXmlConverter();
            final DocumentAccessor documentAccessor = getDocumentAccessor();
            final Document sourceDocument = documentXmlConverter.loadDocument(sourceXml);
            final Node embeddedElement = documentAccessor.getNode(sourceDocument, xPathExprEmbeddedElement)
                    .orElseThrow(() -> new DocumentSegmentationException("Embedded Element does not exist in the Source XML"));

            // Create new document
            final Document newXmlDocument = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            // Import ClinicalDocument node to the new document
            final Node copyNode = newXmlDocument.importNode(
                    embeddedElement, true);
            // Add ClinicalDocument node as the root node to the document
            newXmlDocument.appendChild(copyNode);
            return documentXmlConverter.convertXmlDocToString(newXmlDocument);
        } catch (XPathExpressionException | ParserConfigurationException
                | DocumentXmlConverterException e) {
            throw new DocumentSegmentationException(e);
        }
    }
}
