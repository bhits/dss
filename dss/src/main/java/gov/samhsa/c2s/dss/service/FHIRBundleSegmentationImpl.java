package gov.samhsa.c2s.dss.service;

import ca.uhn.fhir.parser.XmlParser;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.brms.service.RuleExecutionService;
import gov.samhsa.c2s.brms.service.dto.AssertAndExecuteClinicalFactsResponse;
import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerException;
import gov.samhsa.c2s.common.util.StringURIResolver;
import gov.samhsa.c2s.dss.infrastructure.valueset.ValueSetService;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ConceptCodeAndCodeSystemOidDto;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetCategoryMapResponseDto;
import gov.samhsa.c2s.dss.service.dto.DSSRequestForFHIR;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFHIR;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.c2s.dss.service.fhir.EmbeddedFHIRBundleExtractor;
import gov.samhsa.c2s.dss.service.fhir.FHIRBundleRedactor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.InstantType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.transform.URIResolver;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FHIRBundleSegmentationImpl implements FHIRBundleSegmentation {

    private static final String EXTRACT_CLINICAL_FACTS_FOR_FHIRBUNDLE_XSL = "extractClinicalFactsForFHIRBundle.xsl";
    private static final String TAG_FOR_FHIRBUNDLE_XSL = "tagForFHIRBundle.xsl";

    private static final String PARAM_XACML_RESULT = "xacmlResult";

    private final Logger logger = LoggerFactory.getLogger(this);

    @Autowired
    private XmlParser fhirXmlParser;

    @Autowired
    private SimpleMarshaller marshaller;

    @Autowired
    private XmlTransformer xmlTransformer;

    @Autowired
    private ValueSetService valueSetService;

    @Autowired
    private RuleExecutionService ruleExecutionService;

    @Autowired
    private EmbeddedFHIRBundleExtractor embeddedFHIRBundleExtractor;

    @Autowired
    private FHIRBundleRedactor fhirBundleRedactor;

    @Override
    public DSSResponseForFHIR segmentFHIRBundle(DSSRequestForFHIR dssRequestForFHIR) {
        try {
            final XacmlResult xacmlResult = dssRequestForFHIR.getXacmlResult();
            final String xacmlResultXml = marshal(xacmlResult);
            final Optional<URIResolver> uriResolver = Optional
                    .of(new StringURIResolver()
                            .put(PARAM_XACML_RESULT, xacmlResultXml));

            final Bundle fhirBundle = dssRequestForFHIR.getStu3FHIRBundle();

//            // Validate the original Bundle
//            final ValidationResult validationResult = fhirValidator.validateWithResult(fhirBundle);
//            Assert.isTrue(validationResult.isSuccessful(), "FHIR validation is failed for the original bundle with " + validationResult.getMessages().size() + " messages");

            // Convert FHIR Bundle to XML
            final String originalFhirBundleXml = fhirXmlParser.encodeResourceToString(fhirBundle);
            logger.debug(() -> "originalFhirBundleXml: " + originalFhirBundleXml);

            final String factModelXml = xmlTransformer.transform(
                    originalFhirBundleXml, new ClassPathResource(EXTRACT_CLINICAL_FACTS_FOR_FHIRBUNDLE_XSL).getURI().toString(),
                    Optional.empty(), uriResolver);
            logger.debug(() -> "factModelXml: " + factModelXml);

            // Get clinical bundleWithGeneratedIds with generatedEntryId elements
            final String bundleWithGeneratedIds = embeddedFHIRBundleExtractor.extractFHIRBundleFromFactModel(factModelXml);
            logger.debug(() -> "bundleWithGeneratedIds: " + bundleWithGeneratedIds);

            final String cleanFactModel = fhirBundleRedactor.cleanUpEmbeddedFHIRBundleFromFactModel(factModelXml);
            logger.debug(() -> "cleanFactModel: " + cleanFactModel);

            final FactModel factModel = marshaller.unmarshalFromXml(FactModel.class, cleanFactModel);
            logger.debug(factModel::toString);

            // Get and set value set categories to clinical facts
            final List<ConceptCodeAndCodeSystemOidDto> conceptCodeAndCodeSystemOidDtoList =
                    factModel.getClinicalFactList().stream()
                            .map(fact -> new ConceptCodeAndCodeSystemOidDto(fact.getCode(), fact.getCodeSystem()))
                            .collect(Collectors.toList());
            // Get value set categories
            final List<ValueSetCategoryMapResponseDto> valueSetCategories = valueSetService
                    .lookupValueSetCategories(conceptCodeAndCodeSystemOidDtoList);
            factModel.getClinicalFactList()
                    .stream()
                    .forEach(fact -> valueSetCategories.stream()
                            .filter(dto -> fact.getCode().equals(dto.getCodedConceptCode()) && fact.getCodeSystem().equals(dto.getCodeSystemOid()))
                            //TODO: this should also return system as well as the code for the value set categories
                            .map(ValueSetCategoryMapResponseDto::getValueSetCategoryCodes)
                            .filter(Objects::nonNull)
                            .findAny().ifPresent(fact::setValueSetCategories));
            logger.debug(factModel::toString);
            // get execution response container
            final AssertAndExecuteClinicalFactsResponse brmsResponse = ruleExecutionService
                    .assertAndExecuteClinicalFacts(factModel);
            String executionResponseContainer = brmsResponse
                    .getRuleExecutionResponseContainer();
            final String rulesFired = brmsResponse.getRulesFired();
            logger.debug(() -> "rulesFired: " + rulesFired);

            // unmarshall from originalFhirBundleXml to RuleExecutionContainer
            final RuleExecutionContainer ruleExecutionContainer = marshaller.unmarshalFromXml(
                    RuleExecutionContainer.class, executionResponseContainer);

            logger.debug(() -> "Fact model: " + factModelXml);
            logger.debug(() -> "Rule Execution Container size: "
                    + ruleExecutionContainer.getExecutionResponseList().size());
            logger.debug(() -> "ruleExecutionContainer: " + ruleExecutionContainer);

            // start tagging the bundle with sensitivity categories
            final String finalFactModelXml = marshal(factModel);
            logger.debug(() -> "finalFactModelXml: " + finalFactModelXml);
            final String ruleExecutionContainerXml = marshal(ruleExecutionContainer);
            logger.debug(() -> "ruleExecutionContainerXml: " + ruleExecutionContainerXml);
            final Optional<URIResolver> uriResolverForTagging = Optional
                    .of(new StringURIResolver()
                            .put("factModel", finalFactModelXml.replace(
                                    "<FactModel>", "<FactModel xmlns=\"http://hl7.org/fhir\">"))
                            .put("ruleExecutionResponseContainer", ruleExecutionContainerXml.replace(
                                    "<ruleExecutionContainer>", "<ruleExecutionContainer xmlns=\"http://hl7.org/fhir\">")));
            final String taggedBundleXml = xmlTransformer.transform(bundleWithGeneratedIds,
                    new ClassPathResource(TAG_FOR_FHIRBUNDLE_XSL).getURI().toString(), Optional.empty(), uriResolverForTagging);
            logger.debug(() -> "taggedBundleXml: " + taggedBundleXml);

            final String cleanedUpTaggedBundleXml = fhirBundleRedactor.cleanUpGeneratedEntryIds(taggedBundleXml);
            logger.debug(() -> "cleanedUpTaggedBundleXml: " + cleanedUpTaggedBundleXml);

            // Update `Bundle.meta.lastUpdated` and `Bundle.id`
            final Bundle taggedBundle = (Bundle) fhirXmlParser.parseResource(cleanedUpTaggedBundleXml);
            taggedBundle.setId(UUID.randomUUID().toString());
            taggedBundle.getMeta().setLastUpdatedElement(InstantType.now());

//            // Validate the segmented Bundle
//            final ValidationResult validationResult = fhirValidator.validateWithResult(taggedBundle);
//            Assert.isTrue(validationResult.isSuccessful(), "FHIR validation is failed for the segmented bundle with " + validationResult.getMessages().size() + " messages");

            return DSSResponseForFHIR.of(taggedBundle);
        } catch (IOException | SimpleMarshallerException e) {
            throw new DocumentSegmentationException(e.getMessage(), e);
        }
    }

    private String marshal(Object o) {
        try {
            return marshaller.marshal(o);
        } catch (SimpleMarshallerException e) {
            throw new DocumentSegmentationException(e);
        }
    }
}
