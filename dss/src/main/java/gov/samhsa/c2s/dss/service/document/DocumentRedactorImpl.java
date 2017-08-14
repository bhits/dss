package gov.samhsa.c2s.dss.service.document;

import feign.FeignException;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.dss.config.RedactionHandlerIdentityConfig;
import gov.samhsa.c2s.dss.infrastructure.valueset.ValueSetService;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetCategoryResponseDto;
import gov.samhsa.c2s.dss.service.document.dto.RedactedDocument;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractDocumentLevelRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractObligationLevelRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractRedactionHandler;
import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.c2s.dss.service.exception.VssServiceUnreachableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
public class DocumentRedactorImpl implements DocumentRedactor {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    /**
     * The marshaller.
     */
    @Autowired
    private SimpleMarshaller marshaller;

    /**
     * The document xml converter.
     */
    @Autowired
    private DocumentXmlConverter documentXmlConverter;

    /**
     * The document accessor.
     */
    @Autowired
    private DocumentAccessor documentAccessor;

    /**
     * The document level redaction handlers.
     */
    @Autowired
    private Set<AbstractDocumentLevelRedactionHandler> documentLevelRedactionHandlers;

    /**
     * The obligation level redaction handlers.
     */
    @Autowired
    private Set<AbstractObligationLevelRedactionHandler> obligationLevelRedactionHandlers;

    /**
     * The clinical fact level redaction handlers.
     */
    @Autowired
    private Set<AbstractClinicalFactLevelRedactionHandler> clinicalFactLevelRedactionHandlers;

    /**
     * The post redaction level redaction handlers.
     */
    @Autowired
    private Set<AbstractPostRedactionLevelRedactionHandler> postRedactionLevelRedactionHandlers;

    @Autowired
    private ValueSetService valueSetService;

    public DocumentRedactorImpl() {
    }

    /**
     * Instantiates a new document redactor impl.
     *
     * @param marshaller                          the marshaller
     * @param documentXmlConverter                the document xml converter
     * @param documentAccessor                    the document accessor
     * @param valueSetService                     the value set service
     * @param documentLevelRedactionHandlers      the document level redaction handlers
     * @param obligationLevelRedactionHandlers    the obligation level redaction handlers
     * @param clinicalFactLevelRedactionHandlers  the clinical fact level redaction handlers
     * @param postRedactionLevelRedactionHandlers the post redaction level redaction handlers
     */
    @Autowired
    public DocumentRedactorImpl(
            SimpleMarshaller marshaller,
            DocumentXmlConverter documentXmlConverter,
            DocumentAccessor documentAccessor,
            ValueSetService valueSetService,
            Set<AbstractDocumentLevelRedactionHandler> documentLevelRedactionHandlers,
            Set<AbstractObligationLevelRedactionHandler> obligationLevelRedactionHandlers,
            Set<AbstractClinicalFactLevelRedactionHandler> clinicalFactLevelRedactionHandlers,
            Set<AbstractPostRedactionLevelRedactionHandler> postRedactionLevelRedactionHandlers) {
        super();
        this.marshaller = marshaller;
        this.documentXmlConverter = documentXmlConverter;
        this.documentAccessor = documentAccessor;
        this.valueSetService = valueSetService;
        this.documentLevelRedactionHandlers = documentLevelRedactionHandlers;
        this.obligationLevelRedactionHandlers = obligationLevelRedactionHandlers;
        this.clinicalFactLevelRedactionHandlers = clinicalFactLevelRedactionHandlers;
        this.postRedactionLevelRedactionHandlers = postRedactionLevelRedactionHandlers;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        logger.info(() -> "Loaded redaction handlers (excluding " + RedactionHandlerIdentityConfig.IDENTITY + "s): "
                + (documentLevelRedactionHandlers.stream()
                .map(AbstractRedactionHandler::toString)
                .filter(this::isNotIdentity)
                .count()
                + obligationLevelRedactionHandlers.stream()
                .map(AbstractRedactionHandler::toString)
                .filter(this::isNotIdentity)
                .count()
                + clinicalFactLevelRedactionHandlers.stream()
                .map(AbstractRedactionHandler::toString)
                .filter(this::isNotIdentity)
                .count()
                + postRedactionLevelRedactionHandlers.stream()
                .map(AbstractRedactionHandler::toString)
                .filter(this::isNotIdentity)
                .count())
        );
        logger.info(() -> "documentLevelRedactionHandlers: " + documentLevelRedactionHandlers.toString());
        logger.info(() -> "obligationLevelRedactionHandlers: " + obligationLevelRedactionHandlers.toString());
        logger.info(() -> "clinicalFactLevelRedactionHandlers: " + clinicalFactLevelRedactionHandlers.toString());
        logger.info(() -> "postRedactionLevelRedactionHandlers: " + postRedactionLevelRedactionHandlers.toString());
    }

    @Override
    public String cleanUpEmbeddedClinicalDocumentFromFactModel(
            String factModelXml) {
        final String xPathExprEmbeddedClinicalDocument = "//hl7:EmbeddedClinicalDocument";
        return cleanUpElements(factModelXml, xPathExprEmbeddedClinicalDocument);
    }

    @Override
    public String cleanUpGeneratedEntryIds(String document) {
        // Remove all generatedEntryId elements to clean up the clinical document
        final String xPathExprGeneratedEntryId = "//hl7:generatedEntryId";
        return cleanUpElements(document, xPathExprGeneratedEntryId);
    }

    @Override
    public String cleanUpGeneratedServiceEventIds(String document) {
        // Remove all generatedServiceEventId elements to clean up the clinical document
        final String xPathExprGeneratedEntryId = "//hl7:generatedServiceEventId";
        return cleanUpElements(document, xPathExprGeneratedEntryId);
    }

    @Override
    public RedactedDocument redactDocument(String document,
                                           RuleExecutionContainer ruleExecutionContainer,
                                           FactModel factModel,
                                           String documentType) {
        String tryPolicyDocument = null;
        RedactionHandlerResult combinedResults;
        final XacmlResult xacmlResult = factModel.getXacmlResult();

        Set<String> xacmlPdpObligations = new HashSet<>(xacmlResult.getPdpObligations());

        Set<ValueSetCategoryResponseDto> allValueSetCategoryDtosSet = new HashSet<>();

        try {
            allValueSetCategoryDtosSet = new HashSet<>(valueSetService.getAllValueSetCategories());
        } catch (FeignException e) {
            logger.error("A FeignException occurred while trying to call valueSetService.getAllValueSetCategories.");
            logger.debug("FeignException Details: " + e.getMessage());
            logger.debug("FeignException Stack Trace: " + e.toString());

            throw new VssServiceUnreachableException("Unable to contact Value Set Service endpoint");
        }

        Set<String> pdpObligationsComplementSet = new HashSet<>();

        // Calculate the set difference (i.e. complement set)
        pdpObligationsComplementSet.addAll(allValueSetCategoryDtosSet.stream()
                .map(ValueSetCategoryResponseDto::getCode)
                .filter(valSetCatCode -> !xacmlPdpObligations.contains(valSetCatCode))
                .collect(toList()));

        PdpObligationsComplementSetDto pdpObligationsComplementSetDto = new PdpObligationsComplementSetDto(pdpObligationsComplementSet);

        try {
            final Document xmlDocument = documentXmlConverter.loadDocument(document);
            final Document factModelDocument = documentXmlConverter
                    .loadDocument(marshaller.marshal(factModel));

            // DOCUMENT LEVEL REDACTION HANDLERS
            final RedactionHandlerResult documentLevelResults = documentLevelRedactionHandlers
                    .stream()
                    .map(handler -> handler.execute(xmlDocument, documentType))
                    .reduce(RedactionHandlerResult::concat)
                    .orElseGet(RedactionHandlerResult::new);

            // OBLIGATION LEVEL REDACTION HANDLERS
            final RedactionHandlerResult obligationLevelResults = xacmlResult.getPdpObligations().stream()
                    .flatMap(obligation -> obligationLevelRedactionHandlers
                            .stream()
                            .map(handler -> handler.execute(xmlDocument, xacmlResult, factModel,
                                    factModelDocument, ruleExecutionContainer, obligation,
                                    pdpObligationsComplementSetDto)))
                    .reduce(RedactionHandlerResult::concat)
                    .orElseGet(RedactionHandlerResult::new);

            // CLINICAL FACT LEVEL REDACTION HANDLERS
            final RedactionHandlerResult clinicalFactLevelResults = factModel.getClinicalFactList().stream()
                    .flatMap(fact -> clinicalFactLevelRedactionHandlers
                            .stream()
                            .map(handler -> handler.execute(xmlDocument, xacmlResult, factModel,
                                    factModelDocument, fact, ruleExecutionContainer,
                                    pdpObligationsComplementSetDto)))
                    .reduce(RedactionHandlerResult::concat)
                    .orElseGet(RedactionHandlerResult::new);

            combinedResults = RedactionHandlerResult.empty()
                    .concat(documentLevelResults)
                    .concat(obligationLevelResults)
                    .concat(clinicalFactLevelResults);

            // Create tryPolicyDocument before the actual redacting
            tryPolicyDocument = documentXmlConverter
                    .convertXmlDocToString(xmlDocument);

            // REDACTION
            // Redact all nodes in redactNodeList
            // (sections, entries, text nodes)
            redactNodesIfNotNull(combinedResults.getRedactNodeList());

            // POST REDACTION LEVEL REDACTION HANDLERS
            postRedactionLevelRedactionHandlers.forEach(handler ->
                    handler.execute(xmlDocument, xacmlResult, factModel, factModelDocument,
                            ruleExecutionContainer, combinedResults,
                            pdpObligationsComplementSetDto, documentType));

            // Convert redacted document to xml string
            document = documentXmlConverter.convertXmlDocToString(xmlDocument);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        }
        return new RedactedDocument(document, tryPolicyDocument,
                combinedResults.getRedactSectionSet(), combinedResults.getRedactCategorySet());
    }

    @Override
    public DocumentXmlConverter getDocumentXmlConverter() {
        return this.documentXmlConverter;
    }

    @Override
    public DocumentAccessor getDocumentAccessor() {
        return this.documentAccessor;
    }

    private boolean isNotIdentity(String name) {
        return !RedactionHandlerIdentityConfig.IDENTITY.equals(name);
    }
}
