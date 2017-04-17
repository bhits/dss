package gov.samhsa.c2s.dss.service.document.redact.base;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import org.w3c.dom.Document;

/**
 * The Class AbstractObligationLevelRedactionHandler.
 */
public abstract class AbstractObligationLevelRedactionHandler extends AbstractRedactionHandler {

    /**
     * Instantiates a new abstract obligation level callback.
     *
     * @param documentAccessor the document accessor
     */
    public AbstractObligationLevelRedactionHandler(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    /**
     * Execute.
     *
     * @param xmlDocument            the xml document
     * @param xacmlResult            the xacml result
     * @param factModel              the fact model
     * @param factModelDocument      the fact model document
     * @param ruleExecutionContainer the rule execution container
     * @param obligationValue        the obligation value
     * @return RedactionHandlerResult
     */
    public abstract RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                                   FactModel factModel, Document factModelDocument,
                                                   RuleExecutionContainer ruleExecutionContainer,
                                                   String obligationValue);
}
