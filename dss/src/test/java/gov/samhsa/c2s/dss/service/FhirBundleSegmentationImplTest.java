package gov.samhsa.c2s.dss.service;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.brms.domain.SubjectPurposeOfUse;
import gov.samhsa.c2s.brms.service.RuleExecutionService;
import gov.samhsa.c2s.brms.service.dto.AssertAndExecuteClinicalFactsResponse;
import gov.samhsa.c2s.common.audit.AuditClientImpl;
import gov.samhsa.c2s.common.audit.AuditVerb;
import gov.samhsa.c2s.common.audit.PredicateKey;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.c2s.common.filereader.FileReader;
import gov.samhsa.c2s.common.filereader.FileReaderImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.common.validation.XmlValidation;
import gov.samhsa.c2s.common.validation.XmlValidationResult;
import gov.samhsa.c2s.common.validation.exception.InvalidXmlDocumentException;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.c2s.dss.infrastructure.valueset.ValueSetServiceImplMock;
import gov.samhsa.c2s.dss.service.document.*;
import gov.samhsa.c2s.dss.service.document.dto.RedactedDocument;
import gov.samhsa.c2s.dss.service.dto.DSSRequest;
import gov.samhsa.c2s.dss.service.dto.DSSRequestForFhir;
import gov.samhsa.c2s.dss.service.dto.DSSResponse;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFhir;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import gov.samhsa.c2s.dss.service.metadata.AdditionalMetadataGeneratorForSegmentedClinicalDocumentImpl;
import gov.samhsa.c2s.dss.service.metadata.MetadataGeneratorImpl;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FhirBundleSegmentationImplTest {

    private static final String FHIR_SEARCHSET_TYPE = "searchset";
    private static final String FHIR_REFERENCE = "reference";
    private static final String FHIR_SUBJECT = "subject";

    private static final String FHIR_CONFIDENTIALITY_CODE_V = "V";
    private static final String FHIR_CONFIDENTIALITY_CODE_R = "R";
    private static final String FHIR_CONFIDENTIALITY_CODE_N = "N";
    private static final String FHIR_SYSTEM_CONFIDENTIALITY = "http://hl7.org/fhir/v3/Confidentiality";
    private static final String PARAM_XACML_RESULT = "xacmlResult";

    private static SimpleMarshallerImpl marshaller;
    private static SimpleMarshallerImpl marshallerMock;

    FhirBundleSegmentationImpl fhirBundleSegmentation;
    @Before
    public void setUp() throws Exception {
        fhirBundleSegmentation = new FhirBundleSegmentationImpl();
   }

//    @Test
//    void unit_test_bundleConfidentialities(){
//    }

    // TODO to be finished
    @Ignore
    @Test
    public void test_fhir_bundle_segmentation()throws Exception  {

        DSSRequestForFhir dssRequestForFhir = createDssRequestForFhir();

        // Marshaller mock
        marshallerMock = mock(SimpleMarshallerImpl.class);
        when(marshallerMock.marshal(dssRequestForFhir.getXacmlResult())).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><xacmlResult><pdpDecision>PERMIT</pdpDecision><messageId>12345</messageId><pdpObligation>ETH</pdpObligation><pdpObligation>GDIS</pdpObligation><pdpObligation>HIV</pdpObligation><pdpObligation>PSY</pdpObligation><pdpObligation>SEX</pdpObligation><pdpObligation>ALC</pdpObligation><pdpObligation>COM</pdpObligation><pdpObligation>ADD</pdpObligation><patientId>testPatientId</patientId></xacmlResult>");

        DSSResponseForFhir dssResponseForFhir = fhirBundleSegmentation.segmentFhirBundle(dssRequestForFhir);
        Bundle fhirBundle = dssRequestForFhir.getFhirStu3Bundle();

    }

    private DSSRequestForFhir createDssRequestForFhir(){
        DSSRequestForFhir dssRequestForFhir =  new DSSRequestForFhir();

        XacmlResult xacmlResult = new XacmlResult();
        xacmlResult.setPdpDecision("PERMIT");
        SubjectPurposeOfUse purposeOfUse = SubjectPurposeOfUse.fromValue("HEALTHCARE_TREATMENT");
        xacmlResult.setSubjectPurposeOfUse(purposeOfUse);

        final String[] o = {"ETH", "GDIS", "HIV", "PSY", "SEX","ALC", "COM", "ADD"};
        xacmlResult.setPdpObligations( Arrays.asList(o));

        xacmlResult.setPatientId("testPatientId");
        xacmlResult.setMessageId("12345");

        dssRequestForFhir.setXacmlResult(xacmlResult);
        dssRequestForFhir.setEnableTryPolicyResponse(Optional.of(true));

        dssRequestForFhir.setFhirStu3Bundle(createFhirBundle());

        return dssRequestForFhir;
    }
    private Bundle createFhirBundle(){
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(2);
        Meta meta = new Meta();
        meta.setLastUpdated(new Date());
        bundle.setMeta(meta);

        List<Bundle.BundleLinkComponent> links = new ArrayList<>();

        Bundle.BundleLinkComponent linkItem = new Bundle.BundleLinkComponent();
        linkItem.setRelation("self");
        linkItem.setUrl("https://fhirtest.uhn.ca/baseDstu2/Patient/13275/$everything?_format=json");
        links.add(linkItem);

        Bundle.BundleLinkComponent linkItem1 = new Bundle.BundleLinkComponent();
        linkItem1.setRelation("next");
        linkItem1.setUrl("https://fhirtest.uhn.ca/baseDstu2?_getpages=3bf5f8ea-69c8-4de0-84cf-db09d44adfc7&_getpagesoffset=10&_count=10&_format=json&_pretty=true&_bundletype=searchset");
        links.add(linkItem1);

        bundle.setLink(links);

        List<Bundle.BundleEntryComponent> entries =  new ArrayList<>();

        Bundle.BundleEntryComponent entryComponent =  new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("https://fhirtest.uhn.ca/baseDstu2/Observation/13280");

        // Observation 1
        Observation  observation = new Observation();
        observation.setId("13280");

        Meta resourceMeta = new Meta();
        resourceMeta.setVersionId("1");
        resourceMeta.setLastUpdated(new Date());
        observation.setMeta(resourceMeta);
//        Observation.ObservationStatus observationStatus = new Observation.ObservationStatus();
//        observation.setStatus(ObservationStatusEnum.FINAL);

        observation.setSubject(new Reference().setReference( "Patient/13275"));

        List<Observation.ObservationComponentComponent> observationComponentComponents =  new ArrayList<>();
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org","55423-8", "Number of steps" ));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org","55411-3", "Exercise duration" ));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org","55423-8", "Number of steps" ));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org","55411-3", "Exercise duration" ));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org","66267-6", "Time Period" ));

        observation.setComponent(observationComponentComponents);
        entryComponent.setResource(observation);

        // Observation 2
        Observation  observation2 = new Observation();
        observation2.setId("13276");
        observation2.setMeta(resourceMeta);
        observation2.setSubject(new Reference().setReference( "Patient/13275"));

        List<Observation.ObservationComponentComponent> observationComponentComponents2 =  new ArrayList<>();
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org","55423-8", "Number of steps" ));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org","55411-3", "Exercise duration" ));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org","55423-8", "Number of steps" ));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org","55411-3", "Exercise duration" ));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org","66267-6", "Time Period" ));

        observation2.setComponent(observationComponentComponents2);

        entryComponent.setResource(observation2);
        entries.add(entryComponent);

        bundle.setEntry(entries);

        return bundle;
    }

    private Observation.ObservationComponentComponent createObservationComponentComponent(String system, String code, String display){
        Observation.ObservationComponentComponent componentComponent =  new Observation.ObservationComponentComponent();
        componentComponent.setCode( createCoding(system,code, display ));
        return componentComponent;
    }
    private CodeableConcept createCoding(String system, String code, String display){
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding().setSystem(system).setCode(code).setDisplay(display);
        return codeableConcept;
    }
}

