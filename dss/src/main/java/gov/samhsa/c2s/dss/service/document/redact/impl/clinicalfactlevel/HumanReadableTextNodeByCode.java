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
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import java.util.Optional;

@Service
public class HumanReadableTextNodeByCode extends AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_HUMAN_READABLE_TEXT_NODE.
     */
    private static final String XPATH_HUMAN_READABLE_TEXT_NODE = "//hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text//*/text()[contains(lower-case(.), '%2')]";

    /**
     * Instantiates a new document node collector for human readable text node
     * by code.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public HumanReadableTextNodeByCode(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                          FactModel factModel, Document factModelDocument, ClinicalFact fact,
                                          RuleExecutionContainer ruleExecutionContainer) {
        return Optional.ofNullable(fact.getCode())
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .flatMap(code -> findMatchingCategoryAsOptional(xacmlResult, fact)
                        .map(foundCategory -> addNodesToListForSensitiveCategory(
                                foundCategory, xmlDocument,
                                XPATH_HUMAN_READABLE_TEXT_NODE,
                                fact.getEntry(), code)))
                .orElseGet(RedactionHandlerResult::new);
    }
}
