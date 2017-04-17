package gov.samhsa.c2s.dss.service.document.redact.base;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;

import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public abstract class AbstractRedactionHandler {

    @Autowired
    protected DocumentAccessor documentAccessor;

    private Logger logger = LoggerFactory.getLogger(this);

    /**
     * Instantiates a new abstract callback.
     *
     * @param documentAccessor the document accessor
     */
    public AbstractRedactionHandler(DocumentAccessor documentAccessor) {
        super();
        this.documentAccessor = documentAccessor;
    }

    protected AbstractRedactionHandler() {
    }

    /**
     * Adds the nodes to list.
     *
     * @param xmlDocument the xml document
     * @param xPathExpr   the x path expr
     * @param values      the values
     * @return the RedactionHandlerResult
     */
    protected final RedactionHandlerResult addNodesToList(Document xmlDocument, String xPathExpr, String... values) {
        try {
            Stream<Node> nodeStream = documentAccessor
                    .getNodeListAsStream(xmlDocument, xPathExpr, values);
            final List<Node> listOfNodes = nodeStream
                    .map(this::markRedactForTryPolicyIfElement)
                    .collect(toList());
            RedactionHandlerResult redactionHandlerResult = new RedactionHandlerResult();
            redactionHandlerResult.setRedactNodeList(listOfNodes);
            if (!listOfNodes.isEmpty() && values != null && values.length > 0) {
                redactionHandlerResult.getRedactSectionCodesAndGeneratedEntryIds().add(values[0]);
            }
            return redactionHandlerResult;
        } catch (XPathExpressionException e) {
            throw new RedactionHandlerException(e);
        }
    }

    protected Optional<Node> nullSafeRemove(Node node) {
        Optional<Node> response = Optional.empty();
        if (Objects.nonNull(node)) {
            Node parent = node.getParentNode();
            if (Objects.nonNull(parent)) {
                try {
                    response = Optional.ofNullable(parent.removeChild(node));
                } catch (NullPointerException e) {
                    logger.info(
                            () -> new StringBuilder().append("Node Name: '")
                                    .append(node.getNodeName())
                                    .append("'").append("; Node Value: '")
                                    .append(node.getNodeValue())
                                    .append("'")
                                    .append("; has already been removed.")
                                    .toString(), e);
                }
            }
        }
        return response;
    }

    protected void nullSafeRemoveChildNodes(Node node) {
        Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .map(DocumentAccessor::toNodeStream)
                .map(stream -> stream.collect(toList()))
                .ifPresent(list -> list.forEach(this::nullSafeRemove));
    }

    private Node markRedactForTryPolicyIfElement(Node node) {
        if (Node.ELEMENT_NODE == node.getNodeType()) {
            Element element = (Element) node;
            element.setAttribute("redact", "redact");
        }
        return node;
    }

    /**
     * Contains all.
     *
     * @param categoriesToRedact the list of categories to be redacted based on patient consent
     *                           (Note: This set of categories is the complement set of the categories from the patient's actual consent)
     * @param factCategories     the set of categories associated with the particular clinical fact
     * @return a set of categories associated with the particular clinical fact which are triggering redaction of the fact
     */
    private Set<String> containsAll(List<String> categoriesToRedact, Set<String> factCategories) {
        Set<String> factCategoriesTriggeringRedaction = new HashSet<>();

        if ((categoriesToRedact != null) && (factCategories != null)) {
            if (categoriesToRedact.containsAll(factCategories)) {
                factCategoriesTriggeringRedaction = new HashSet<>(factCategories);
            }
        }

        return factCategoriesTriggeringRedaction;
    }

    /**
     * Find matching categories.
     *
     * @param pdpObligationsComplementSet the pdpObligationsComplementSet
     * @param fact                        the fact
     * @return a set of categories associated with the particular clinical fact which are triggering redaction of the fact
     */
    protected Set<String> findMatchingCategories(PdpObligationsComplementSetDto pdpObligationsComplementSet, ClinicalFact fact) {
        return containsAll(pdpObligationsComplementSet.getAsListPdpObligationsComplementSet(), fact.getValueSetCategories());
    }
}
