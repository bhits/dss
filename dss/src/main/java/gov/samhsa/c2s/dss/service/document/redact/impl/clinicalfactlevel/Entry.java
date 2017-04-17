package gov.samhsa.c2s.dss.service.document.redact.impl.clinicalfactlevel;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class Entry extends AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_ENTRY.
     */
    private static final String XPATH_ENTRY = "//hl7:entry[child::hl7:generatedEntryId[text()='%1']]";

    /**
     * Instantiates a new document node collector for entry.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public Entry(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                          FactModel factModel, Document factModelDocument, ClinicalFact fact,
                                          RuleExecutionContainer ruleExecutionContainer) {
        return findMatchingCategoryAsOptional(xacmlResult, fact)
                .map(foundCategory -> addNodesToListForSensitiveCategory(
                        foundCategory, xmlDocument,
                        XPATH_ENTRY, fact.getEntry()))
                .orElseGet(RedactionHandlerResult::new);
    }
}
