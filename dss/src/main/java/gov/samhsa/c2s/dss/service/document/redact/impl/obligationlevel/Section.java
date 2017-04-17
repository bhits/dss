package gov.samhsa.c2s.dss.service.document.redact.impl.obligationlevel;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractObligationLevelRedactionHandler;

import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

public class Section extends AbstractObligationLevelRedactionHandler {

    /**
     * The Constant XPATH_SECTION.
     */
    private static final String XPATH_SECTION = "//hl7:structuredBody/hl7:component[child::hl7:section[child::hl7:code[@code='%1']]]";

    /**
     * Instantiates a new document node collector for section.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public Section(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument,
                                          RuleExecutionContainer ruleExecutionContainer, String sectionLoincCode, PdpObligationsComplementSetDto pdpObligationsComplementSetDto) {
        // If there is any section with a code exists in pdp obligations,
        // add that section to redactNodeList.
        Assert.notNull(sectionLoincCode, this.getClass().getSimpleName()
                + " requires the c32 section LOINC code to be provided.");
        final RedactionHandlerResult redactionHandlerResult = addNodesToList(xmlDocument, XPATH_SECTION,
                sectionLoincCode);
        if (redactionHandlerResult.getRedactNodeList().size() > 0) {
            redactionHandlerResult.getRedactSectionSet().add(sectionLoincCode);
        }
        return redactionHandlerResult;
    }
}
