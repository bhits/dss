package gov.samhsa.c2s.dss.service.document.redact.impl.clinicalfactlevel;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import java.util.Optional;
import java.util.Set;

@Service
public class HumanReadableContentElementByDisplayName extends AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_DISPLAY_NAME.
     */
    private static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_DISPLAY_NAME = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content//text()[contains(lower-case(.), '%2')]/ancestor::hl7:content";

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE.
     */
    private static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content//text()[contains(lower-case(.), '%2')]/ancestor::hl7:content/following-sibling::node()[position()=1 and self::text()]";

    /**
     * Instantiates a new document node collector for human readable content
     * element by display name.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public HumanReadableContentElementByDisplayName(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument,
                                          ClinicalFact fact, RuleExecutionContainer ruleExecutionContainer, PdpObligationsComplementSetDto pdpObligationsComplementSetDto) {
        return Optional.ofNullable(fact.getDisplayName())
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .map(code -> collectingContentRedactionResults(xmlDocument, xacmlResult, fact, code, pdpObligationsComplementSetDto))
                .orElseGet(RedactionHandlerResult::new);
    }

    private RedactionHandlerResult collectingContentRedactionResults(Document xmlDocument, XacmlResult xacmlResult, ClinicalFact fact, String displayName, PdpObligationsComplementSetDto pdpObligationsComplementSetDto) {
        // Find matching category
        final Set<String> categoriesTriggeringRedaction = findMatchingCategories(pdpObligationsComplementSetDto, fact);

        // Collect the content element
        final RedactionHandlerResult contentElementResult = addNodesToListForSensitiveCategory(
                categoriesTriggeringRedaction, xmlDocument, XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_DISPLAY_NAME, fact.getEntry(), displayName);

        // Collect the text that follows the content element
        // (if exists)s
        final RedactionHandlerResult nextTextNodeOfContentElementResult = addNodesToListForSensitiveCategory(
                categoriesTriggeringRedaction, xmlDocument, XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE, fact.getEntry(), displayName);

        return contentElementResult.concat(nextTextNodeOfContentElementResult);
    }
}
