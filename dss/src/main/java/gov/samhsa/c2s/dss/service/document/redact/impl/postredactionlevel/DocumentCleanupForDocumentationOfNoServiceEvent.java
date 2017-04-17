package gov.samhsa.c2s.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.stream.Stream;

@Service
public class DocumentCleanupForDocumentationOfNoServiceEvent extends AbstractPostRedactionLevelRedactionHandler {

    /**
     * The Constant XPATH_DOCUMENTATIONOF_WITH_NO_SERVICE_EVENT.
     */
    private static final String XPATH_DOCUMENTATIONOF_WITH_NO_SERVICE_EVENT = "/hl7:ClinicalDocument/hl7:documentationOf[not(hl7:serviceEvent)]";
    private final Logger logger = LoggerFactory.getLogger(this);

    /**
     * Instantiates a new document cleanup for documentation of no service
     * event.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public DocumentCleanupForDocumentationOfNoServiceEvent(
            DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public void execute(Document xmlDocument, XacmlResult xacmlResult,
                        FactModel factModel, Document factModelDocument,
                        RuleExecutionContainer ruleExecutionContainer,
                        RedactionHandlerResult preRedactionResults) {
        try {
            Stream<Node> documentationOfElements = documentAccessor.getNodeListAsStream(
                    xmlDocument, XPATH_DOCUMENTATIONOF_WITH_NO_SERVICE_EVENT);
            documentationOfElements.forEach(this::nullSafeRemove);
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }
}
