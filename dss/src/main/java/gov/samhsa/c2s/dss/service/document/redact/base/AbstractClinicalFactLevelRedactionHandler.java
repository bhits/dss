package gov.samhsa.c2s.dss.service.document.redact.base;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;
import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractClinicalFactLevelRedactionHandler extends AbstractRedactionHandler {

    /**
     * The Constant XPATH_REFERENCES_BY_ENTRY.
     */
    private static final String XPATH_REFERENCES_BY_ENTRY = "//EntryReference[entry='%1']/reference/text()";

    /**
     * Instantiates a new abstract clinical fact level callback.
     *
     * @param documentAccessor the document accessor
     */
    public AbstractClinicalFactLevelRedactionHandler(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    /**
     * Execute.
     *
     * @param xmlDocument                 the xml document
     * @param xacmlResult                 the xacml result
     * @param factModel                   the fact model
     * @param factModelDocument           the fact model document
     * @param fact                        the fact
     * @param ruleExecutionContainer      the rule execution container
     * @param pdpObligationsComplementSet the pdpObligationsComplementSet
     * @return RedactionHandlerResult
     */
    public abstract RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument,
                                                   ClinicalFact fact, RuleExecutionContainer ruleExecutionContainer,
                                                   PdpObligationsComplementSetDto pdpObligationsComplementSet);

    /**
     * Gets the entry reference id node list.
     *
     * @param factModelDocument the fact model document
     * @param fact              the fact
     * @return the entry reference id node list
     */
    protected final NodeList getEntryReferenceIdNodeList(
            Document factModelDocument, ClinicalFact fact) {
        try {
            NodeList references = documentAccessor.getNodeList(factModelDocument,
                    XPATH_REFERENCES_BY_ENTRY, fact.getEntry());
            return references;
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }

    protected final Stream<Node> getEntryReferenceIdNodeListAsStream(
            Document factModelDocument, ClinicalFact fact) {
        try {
            Stream<Node> references = documentAccessor.getNodeListAsStream(factModelDocument,
                    XPATH_REFERENCES_BY_ENTRY, fact.getEntry());
            return references;
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }

    protected RedactionHandlerResult addNodesToListForSensitiveCategory(Set<String> categoriesTriggeringRedaction,
                                                                        Document xmlDocument, String xPathExpr, String... values) {
        RedactionHandlerResult redactionHandlerResult = new RedactionHandlerResult();

        if (categoriesTriggeringRedaction.size() > 0) {
            redactionHandlerResult = addNodesToList(xmlDocument, xPathExpr, values);
            redactionHandlerResult.getRedactCategorySet().addAll(categoriesTriggeringRedaction);
        }

        return redactionHandlerResult;
    }
}
