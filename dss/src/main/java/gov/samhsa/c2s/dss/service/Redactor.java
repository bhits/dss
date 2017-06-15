package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public interface Redactor extends CoreDocumentUtilitiesAware {

    default void redactNodesIfNotNull(List<Node> nodesToBeRedacted) {
        Optional.ofNullable(nodesToBeRedacted)
                .filter(Objects::nonNull)
                .ifPresent(list -> list.forEach(this::redactNodeIfNotNull));
    }

    /**
     * Redact node if not null.
     *
     * @param nodeToBeRedacted the node to be redacted
     */
    default void redactNodeIfNotNull(Node nodeToBeRedacted) {
        if (nodeToBeRedacted != null) {
            /* If displayName contains the code, it will be found twice and can
                already be removed. Therefore, we need to check the parent */
            try {
                nodeToBeRedacted.getParentNode().removeChild(nodeToBeRedacted);
            } catch (final NullPointerException e) {
                LoggerFactory.getLogger(this).info(() -> new StringBuilder()
                        .append("The node value '")
                        .append(nodeToBeRedacted.getNodeValue())
                        .append("' must have been removed already, it cannot be removed again. This might happen if one of the search text contains the other and multiple criteria match to mark the node to be redacted.")
                        .toString());
            }
        }
    }

    default String cleanUpElements(String document, String xPathExpr) {
        try {
            final DocumentXmlConverter documentXmlConverter = getDocumentXmlConverter();
            final DocumentAccessor documentAccessor = getDocumentAccessor();
            final Document xmlDocument = documentXmlConverter.loadDocument(document);
            final List<Node> nodes = documentAccessor.getNodeListAsStream(xmlDocument, xPathExpr).collect(toList());
            redactNodesIfNotNull(nodes);
            return documentXmlConverter.convertXmlDocToString(xmlDocument);
        } catch (final Exception e) {
            LoggerFactory.getLogger(this).error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        }
    }
}
