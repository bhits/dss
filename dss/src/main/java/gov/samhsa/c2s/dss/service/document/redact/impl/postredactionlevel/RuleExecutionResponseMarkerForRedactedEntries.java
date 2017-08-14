package gov.samhsa.c2s.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.RuleExecutionResponse;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.util.Set;
import java.util.function.Predicate;

@Service
public class RuleExecutionResponseMarkerForRedactedEntries extends AbstractPostRedactionLevelRedactionHandler {

    /**
     * Instantiates a new rule execution response marker for redacted entries.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public RuleExecutionResponseMarkerForRedactedEntries(
            DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public void execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument,
                        RuleExecutionContainer ruleExecutionContainer, RedactionHandlerResult preRedactionResults,
                        PdpObligationsComplementSetDto pdpObligationsComplementSetDto, String documentType) {
        // Mark redacted sections and entries in ruleExecutionContainer,
        // so they can be ignored during tagging
        final Set<String> redactSectionCodesAndGeneratedEntryIds = preRedactionResults.getRedactSectionCodesAndGeneratedEntryIds();
        final Predicate<RuleExecutionResponse> sectionOrEntryWillBeRedacted = response ->
                redactSectionCodesAndGeneratedEntryIds.contains(response.getC32SectionLoincCode())
                        || redactSectionCodesAndGeneratedEntryIds.contains(response.getEntry());
        ruleExecutionContainer.getExecutionResponseList()
                .stream()
                .filter(sectionOrEntryWillBeRedacted)
                .forEach(response -> response.setItemAction(RuleExecutionResponse.ITEM_ACTION_REDACT));
    }
}
