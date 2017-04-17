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
import org.w3c.dom.Node;

@Service
public class HumanReadableTableRowById extends AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_HUMAN_READABLE_TABLE_ROW_BY_REFERENCE.
     */
    private static final String XPATH_HUMAN_READABLE_TABLE_ROW_BY_REFERENCE = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:table/hl7:tbody/hl7:tr[descendant-or-self::node()[@ID='%2']]";

    /**
     * Instantiates a new document node collector for human readable table row
     * by id.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public HumanReadableTableRowById(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                          FactModel factModel, Document factModelDocument, ClinicalFact fact,
                                          RuleExecutionContainer ruleExecutionContainer) {
        return findMatchingCategoryAsOptional(xacmlResult, fact)
                .filter(StringUtils::hasText)
                .flatMap(foundCategory ->
                        getEntryReferenceIdNodeListAsStream(factModelDocument, fact)
                                .map(Node::getNodeValue)
                                .filter(StringUtils::hasText)
                                .map(reference -> addNodesToListForSensitiveCategory(
                                        foundCategory, xmlDocument,
                                        XPATH_HUMAN_READABLE_TABLE_ROW_BY_REFERENCE,
                                        fact.getEntry(), reference))
                                .reduce(RedactionHandlerResult::concat))
                .orElseGet(RedactionHandlerResult::new);
    }
}
