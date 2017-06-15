package gov.samhsa.c2s.dss.service;

import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
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
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetCategoryResponseDto;
import gov.samhsa.c2s.dss.service.dto.DSSRequestForFhir;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFhir;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.c2s.dss.service.fhir.EmbeddedFhirBundleExtractor;
import gov.samhsa.c2s.dss.service.fhir.FhirBundleRedactor;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Objects;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Base;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Coding;

import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.xml.transform.URIResolver;
import java.io.IOException;

import java.util.stream.Collectors;

@Service
public class FhirBundleSegmentationImpl implements FhirBundleSegmentation {

    private static final String EXTRACT_CLINICAL_FACTS_FOR_FHIR_BUNDLE_XSL = "extractClinicalFactsForFHIRBundle.xsl";
    private static final String TAG_FOR_FHIR_BUNDLE_XSL = "tagForFHIRBundle.xsl";

    private static final String PARAM_XACML_RESULT = "xacmlResult";
    private static final String FHIR_SEARCHSET_TYPE = "searchset";
    private static final String FHIR_REFERENCE = "reference";
    private static final String FHIR_SUBJECT = "subject";

    private static final String FHIR_CONFIDENTIALITY_CODE_V = "V";
    private static final String FHIR_CONFIDENTIALITY_CODE_R = "R";
    private static final String FHIR_CONFIDENTIALITY_CODE_N = "N";
    private static final String FHIR_SYSTEM_CONFIDENTIALITY = "http://hl7.org/fhir/v3/Confidentiality";

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
    private EmbeddedFhirBundleExtractor embeddedFhirBundleExtractor;

    @Autowired
    private FhirBundleRedactor fhirBundleRedactor;

    @Autowired
    private FhirValidator fhirValidator;

    @Override
    public DSSResponseForFhir segmentFhirBundle(DSSRequestForFhir dssRequestForFhir) {
        try {
            final XacmlResult xacmlResult = dssRequestForFhir.getXacmlResult();
            final String xacmlResultXml = marshal(xacmlResult);
            final Optional<URIResolver> uriResolver = Optional
                    .of(new StringURIResolver()
                            .put(PARAM_XACML_RESULT, xacmlResultXml));

            final Bundle fhirBundle = dssRequestForFhir.getFhirStu3Bundle();

            // Assumption: Ensure bundle type is SearchSet
            assertIsSearchSetBundle(fhirBundle);

            //Assumption: Ensure bundle contains resources for one patient
            assertIsSinglePatientPerBundle(fhirBundle);

            // Validate bundle
            validateBundleIfEnabled(dssRequestForFhir.getEnableBundleValidation().orElse(false), fhirBundle);

            // Convert FHIR Bundle to XML
            final String originalFhirBundleXml = fhirXmlParser.encodeResourceToString(fhirBundle);
            logger.debug(() -> "originalFhirBundleXml: " + originalFhirBundleXml);

            final String factModelXml = xmlTransformer.transform(
                    originalFhirBundleXml, new ClassPathResource(EXTRACT_CLINICAL_FACTS_FOR_FHIR_BUNDLE_XSL).getURI().toString(),
                    Optional.empty(), uriResolver);

            logger.debug(() -> "factModelXml: " + factModelXml);

            // Get clinical bundleWithGeneratedIds with generatedEntryId elements
            final String bundleWithGeneratedIds = embeddedFhirBundleExtractor.extractFhirBundleFromFactModel(factModelXml);
            logger.debug(() -> "bundleWithGeneratedIds: " + bundleWithGeneratedIds);

            final String cleanFactModel = fhirBundleRedactor.cleanUpEmbeddedFhirBundleFromFactModel(factModelXml);
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
            logger.debug(() -> "Rule Execution Container size: " + ruleExecutionContainer.getExecutionResponseList().size());
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
                    new ClassPathResource(TAG_FOR_FHIR_BUNDLE_XSL).getURI().toString(), Optional.empty(), uriResolverForTagging);

            logger.debug(() -> "taggedBundleXml: " + taggedBundleXml);

            final String cleanedUpTaggedBundleXml = fhirBundleRedactor.cleanUpGeneratedEntryIds(taggedBundleXml);

            logger.debug(() -> "cleanedUpTaggedBundleXml: " + cleanedUpTaggedBundleXml);

            // Update `Bundle.meta.lastUpdated` and `Bundle.id`
            final Bundle taggedBundle = recreateBundle(cleanedUpTaggedBundleXml);

            // Validate bundle after tagging
            validateBundleIfEnabled(dssRequestForFhir.getEnableBundleValidation().orElse(false), taggedBundle);

            if (isRedactionEnabled(dssRequestForFhir)) {
                dssRequestForFhir.setFhirStu3Bundle(taggedBundle);
                Bundle redactedFhirBundle = redactFhirBundle(taggedBundle, dssRequestForFhir.getXacmlResult());
                updateBundleMetaInformation(redactedFhirBundle);
                updateConfidentiality(redactedFhirBundle);
               return  DSSResponseForFhir.of(redactedFhirBundle);
            }else{
                updateBundleMetaInformation(taggedBundle);
                return DSSResponseForFhir.of(taggedBundle);
            }
        } catch (IOException | SimpleMarshallerException e) {
            throw new DocumentSegmentationException(e.getMessage(), e);
        }
    }

    boolean isRedactionEnabled(DSSRequestForFhir dssRequestForFhir){
        return dssRequestForFhir.getEnableRedact().orElse(false);
    }

    private Bundle recreateBundle(final String cleanedUpTaggedBundleXml ){
        return (Bundle) fhirXmlParser.parseResource(cleanedUpTaggedBundleXml);
    }

    private void updateBundleMetaInformation(Bundle fhirStu3Bundle){
        fhirStu3Bundle.setId(UUID.randomUUID().toString());
        fhirStu3Bundle.getMeta().setLastUpdatedElement(InstantType.now());
        fhirStu3Bundle.setTotal(fhirStu3Bundle.getEntry().size());
    }
    @Override
    public Bundle redactFhirBundle(Bundle fhirbundle,XacmlResult xacmlResult) {

        List<ValueSetCategoryResponseDto> valueSetCategories = valueSetService.getAllValueSetCategories();
        logger.debug(() -> "Entry Size before redaction: " + fhirbundle.getEntry().size());
        List<Bundle.BundleEntryComponent> entriesToBeRedacted = new ArrayList<>();
        fhirbundle.getEntry().stream()
                     .forEach(entry ->{
                         // Determine which security labels are Sensitive
                         List<Coding> securityLabels = entry.getResource().getMeta().getSecurity();
                         // If resource is tagged otherwise ignore
                         if(securityLabels.size() > 0 ){
                             List<Coding> sensitiveSecurityLabels = getSensitiveSecurityLabels(securityLabels, valueSetCategories);
                             // If security label is not sensitive ignore resource
                             if(sensitiveSecurityLabels.size() > 0){
                                 // Create a list of all the codings that do match the PDP obligations
                                     List<String> pdpObligations = xacmlResult.getPdpObligations();
                                 List<Coding> selectCodingForSharing = createListOfCodingsForSharing(sensitiveSecurityLabels,pdpObligations);

                                 // Add entry to list of entries to be redacted if at least one coding does not match the PDP obligation
                                 if(selectCodingForSharing.size() == 0 ){
                                     entriesToBeRedacted.add(entry);
                                 }
                             }
                         }
                     });

        fhirbundle.getEntry().removeAll(entriesToBeRedacted);
        logger.debug(() -> "Entry Size after redaction: " + fhirbundle.getEntry().size());
        return fhirbundle;
    }

    private List<Coding> getSensitiveSecurityLabels(List<Coding> securityLables, List<ValueSetCategoryResponseDto> valueSetCategories){
        List<Coding> sensitiveSecurityLabels = new ArrayList<>();
        securityLables.stream().forEach( coding -> {
            boolean codingIsSensitive = valueSetCategories.stream().anyMatch(valueSetCategory -> (valueSetCategory.getCode().equals(coding.getCode())
                    && valueSetCategory.getSystem().equals(coding.getSystem())));
            if(codingIsSensitive){
                sensitiveSecurityLabels.add(coding);
            }
        });

        return sensitiveSecurityLabels;
    }

    private List<Coding> createListOfCodingsForSharing(List<Coding> sensitiveSecurityLabels, List<String> pdpObligations){
        List<Coding> selectCodingForSharing = new ArrayList<>();
        sensitiveSecurityLabels.stream().forEach(coding -> {
            if(pdpObligations.contains(coding.getCode())){
                selectCodingForSharing.add(coding);
            }
        });

        return selectCodingForSharing;
    }

    @Override
    public DSSResponseForFhir redactAndUpdateFhirBundle(DSSRequestForFhir dssRequestForFhir) {
        // Validate bundle before redaction
        validateBundleIfEnabled(dssRequestForFhir.getEnableBundleValidation().orElse(false), dssRequestForFhir.getFhirStu3Bundle());

        Bundle redactedFhirBundle = redactFhirBundle(dssRequestForFhir.getFhirStu3Bundle(), dssRequestForFhir.getXacmlResult());
        updateBundleMetaInformation(redactedFhirBundle);
        updateConfidentiality(redactedFhirBundle);

        // Validate bundle after redaction
        validateBundleIfEnabled(dssRequestForFhir.getEnableBundleValidation().orElse(false), dssRequestForFhir.getFhirStu3Bundle());

        return  DSSResponseForFhir.of(redactedFhirBundle);
    }

    private void validateBundleIfEnabled(boolean shouldValidate, Bundle fhirStu3Bundle){
        if(shouldValidate){
            assertIsValidateBundle(fhirStu3Bundle);
        }
    }

    private void updateConfidentiality(Bundle fhirStu3Bundle){
        if(containsCondifidentialityCode(FHIR_CONFIDENTIALITY_CODE_V, fhirStu3Bundle)) {
            setBundleConfidentiality(FHIR_CONFIDENTIALITY_CODE_V,fhirStu3Bundle);
        }else if(containsCondifidentialityCode(FHIR_CONFIDENTIALITY_CODE_R, fhirStu3Bundle)){
            setBundleConfidentiality(FHIR_CONFIDENTIALITY_CODE_R,fhirStu3Bundle);
        }else if(containsCondifidentialityCode(FHIR_CONFIDENTIALITY_CODE_N, fhirStu3Bundle)){
            setBundleConfidentiality(FHIR_CONFIDENTIALITY_CODE_N,fhirStu3Bundle);
        }else{
            removeBundleConfidentiality(fhirStu3Bundle);
        }
    }

    private void setBundleConfidentiality(String code, Bundle fhirbundle ){
        fhirbundle.getMeta().getSecurity().stream()
                            .forEach(coding ->{
                                if(coding.getSystem().equals(FHIR_SYSTEM_CONFIDENTIALITY)){
                                    coding.setCode(code);
                                }
                            } );
    }

    private void removeBundleConfidentiality(Bundle fhirbundle ){
        // Get confidentiality codings
        List<Coding> securityCodings =
                fhirbundle.getMeta().getSecurity().stream()
                .filter(coding -> coding.getSystem().equals(FHIR_SYSTEM_CONFIDENTIALITY) )
                .collect(Collectors.toList());
        fhirbundle.getMeta().getSecurity().removeAll(securityCodings);
    }

    private boolean containsCondifidentialityCode(String code,Bundle fhirbundle  ){
        return fhirbundle.getEntry().stream()
                .anyMatch(entry ->
                    entry.getResource().getMeta().getSecurity().stream()
                                        .filter(coding -> coding.getSystem().equals(FHIR_SYSTEM_CONFIDENTIALITY) )
                                        .anyMatch( coding -> coding.getCode().equals(code))
        );
    }

    private void assertIsValidateBundle(Bundle fhirbundle){
        final ValidationResult taggedBundleValidationResult = fhirValidator.validateWithResult(fhirbundle);
        taggedBundleValidationResult.getMessages().stream().forEach(error ->logger.debug(() -> "Error: " + error.getMessage()));
        Assert.isTrue(taggedBundleValidationResult.isSuccessful(), "FHIR validation is failed for the segmented bundle with " + taggedBundleValidationResult.getMessages().size() + " messages");
    }

    private void assertIsSearchSetBundle(Bundle fhirbundle){
        Assert.isTrue(fhirbundle.getType().toCode().equalsIgnoreCase(FHIR_SEARCHSET_TYPE), "Unsupported FHIR bundle type");
    }

    private void assertIsSinglePatientPerBundle(Bundle fhirbundle){
        Set<String> setOfPatientIds = new HashSet<>();
        fhirbundle.getEntry().stream().forEach(entry ->{
                Resource resource = entry.getResource();
                try {
                    List<Base> subjects = resource.listChildrenByName(FHIR_SUBJECT);
                    subjects.stream().forEach(subject ->setOfPatientIds.add(subject.getChildByName(FHIR_REFERENCE).getValues().toString()));
                } catch (FHIRException e) {
                    logger.warn(() -> e.getMessage());
                }
        });
        Assert.isTrue(setOfPatientIds.size() == 1, "Bundle contains resources for more than one patient.");
    }

    private String marshal(Object o) {
        try {
            return marshaller.marshal(o);
        } catch (SimpleMarshallerException e) {
            throw new DocumentSegmentationException(e);
        }
    }
}
