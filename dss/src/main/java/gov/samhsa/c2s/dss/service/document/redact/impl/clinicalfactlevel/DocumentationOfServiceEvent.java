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
public class DocumentationOfServiceEvent extends AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_SERVICE_EVENT.
     */
    private static final String XPATH_SERVICE_EVENT = "/hl7:ClinicalDocument/hl7:documentationOf/hl7:serviceEvent[hl7:generatedServiceEventId[text()='%1']]";

    /**
     * Instantiates a new documentation of service event.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public DocumentationOfServiceEvent(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                          FactModel factModel, Document factModelDocument, ClinicalFact fact,
                                          RuleExecutionContainer ruleExecutionContainer) {
        return findMatchingCategoryAsOptional(xacmlResult, fact)
                .map(foundCategory -> addNodesToListForSensitiveCategory(
                        foundCategory, xmlDocument,
                        XPATH_SERVICE_EVENT, fact.getEntry()))
                .orElseGet(RedactionHandlerResult::new);
    }
}
