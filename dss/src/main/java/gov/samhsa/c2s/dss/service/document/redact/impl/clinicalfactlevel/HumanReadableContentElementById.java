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
import org.w3c.dom.Node;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class HumanReadableContentElementById extends AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_REFERENCE.
     */
    private static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_REFERENCE = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content[@ID='%2']";

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE.
     */
    private static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content[@ID='%2']/following-sibling::node()[position()=1 and self::text()]";

    /**
     * Instantiates a new document node collector for human readable content
     * element by id.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public HumanReadableContentElementById(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument,
                                          ClinicalFact fact, RuleExecutionContainer ruleExecutionContainer, PdpObligationsComplementSetDto pdpObligationsComplementSetDto) {
        Set<String> categoriesTriggeringRedaction = findMatchingCategories(pdpObligationsComplementSetDto, fact);

        return collectingContentRedactionResults(categoriesTriggeringRedaction, xmlDocument, factModelDocument, fact)
                .orElseGet(RedactionHandlerResult::new);
    }

    private Optional<RedactionHandlerResult> collectingContentRedactionResults(Set<String> categoriesTriggeringRedaction, Document xmlDocument,
                                                                               Document factModelDocument, ClinicalFact fact) {
        return getEntryReferenceIdNodeListAsStream(factModelDocument, fact)
                .filter(Objects::nonNull)
                .map(Node::getNodeValue)
                .filter(StringUtils::hasText)
                .map(nodeValue -> addNodesToListForSensitiveCategory(categoriesTriggeringRedaction, xmlDocument,
                        XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_REFERENCE, fact.getEntry(), nodeValue)
                        .concat(addNodesToListForSensitiveCategory(
                                categoriesTriggeringRedaction, xmlDocument,
                                XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE,
                                fact.getEntry(), nodeValue)))
                .reduce(RedactionHandlerResult::concat);
    }
}
