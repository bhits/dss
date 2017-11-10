package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.brms.domain.SubjectPurposeOfUse;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.dss.service.dto.DSSRequestForFhir;
import gov.samhsa.c2s.dss.service.dto.DSSResponseForFhir;
import gov.samhsa.c2s.dss.service.dto.IdentifierDto;
import gov.samhsa.c2s.dss.service.dto.ObligationDto;
import gov.samhsa.c2s.dss.service.dto.XacmlResultDto;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
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

    private static ObligationDto obligation(String code) {
        return ObligationDto.of(SupportedObligationId.SHARE_SENSITIVITY_POLICY_CODE, code);
    }

//    @Test
//    void unit_test_bundleConfidentialities(){
//    }

    @Before
    public void setUp() throws Exception {
        fhirBundleSegmentation = new FhirBundleSegmentationImpl();
    }

    // TODO to be finished
    @Ignore
    @Test
    public void test_fhir_bundle_segmentation() throws Exception {

        DSSRequestForFhir dssRequestForFhir = createDssRequestForFhir();

        // Marshaller mock
        marshallerMock = mock(SimpleMarshallerImpl.class);
        when(marshallerMock.marshal(dssRequestForFhir.getXacmlResult())).thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><xacmlResult><pdpDecision>PERMIT</pdpDecision><messageId>12345</messageId><pdpObligation>ETH</pdpObligation><pdpObligation>GDIS</pdpObligation><pdpObligation>HIV</pdpObligation><pdpObligation>PSY</pdpObligation><pdpObligation>SEX</pdpObligation><pdpObligation>ALC</pdpObligation><pdpObligation>COM</pdpObligation><pdpObligation>ADD</pdpObligation><patientId>testPatientId</patientId></xacmlResult>");

        DSSResponseForFhir dssResponseForFhir = fhirBundleSegmentation.segmentFhirBundle(dssRequestForFhir);
        Bundle fhirBundle = dssRequestForFhir.getFhirStu3Bundle();

    }

    private DSSRequestForFhir createDssRequestForFhir() {
        DSSRequestForFhir dssRequestForFhir = new DSSRequestForFhir();

        XacmlResultDto xacmlResult = new XacmlResultDto();
        xacmlResult.setPdpDecision("PERMIT");
        SubjectPurposeOfUse purposeOfUse = SubjectPurposeOfUse.fromValue("HEALTHCARE_TREATMENT");
        xacmlResult.setSubjectPurposeOfUse(purposeOfUse);

        final String[] o = {"ETH", "GDIS", "HIV", "PSY", "SEX", "ALC", "COM", "ADD"};
        xacmlResult.setPdpObligations(Arrays.stream(o).map(FhirBundleSegmentationImplTest::obligation).collect(toList()));

        xacmlResult.setPatientIdentifier(IdentifierDto.of("homeCommunityId", "testPatientId"));
        xacmlResult.setMessageId("12345");

        dssRequestForFhir.setXacmlResult(xacmlResult);
        dssRequestForFhir.setEnableTryPolicyResponse(Optional.of(true));

        dssRequestForFhir.setFhirStu3Bundle(createFhirBundle());

        return dssRequestForFhir;
    }

    private Bundle createFhirBundle() {
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

        List<Bundle.BundleEntryComponent> entries = new ArrayList<>();

        Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("https://fhirtest.uhn.ca/baseDstu2/Observation/13280");

        // Observation 1
        Observation observation = new Observation();
        observation.setId("13280");

        Meta resourceMeta = new Meta();
        resourceMeta.setVersionId("1");
        resourceMeta.setLastUpdated(new Date());
        observation.setMeta(resourceMeta);
//        Observation.ObservationStatus observationStatus = new Observation.ObservationStatus();
//        observation.setStatus(ObservationStatusEnum.FINAL);

        observation.setSubject(new Reference().setReference("Patient/13275"));

        List<Observation.ObservationComponentComponent> observationComponentComponents = new ArrayList<>();
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org", "55423-8", "Number of steps"));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org", "55411-3", "Exercise duration"));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org", "55423-8", "Number of steps"));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org", "55411-3", "Exercise duration"));
        observationComponentComponents.add(createObservationComponentComponent("http://loinc.org", "66267-6", "Time Period"));

        observation.setComponent(observationComponentComponents);
        entryComponent.setResource(observation);

        // Observation 2
        Observation observation2 = new Observation();
        observation2.setId("13276");
        observation2.setMeta(resourceMeta);
        observation2.setSubject(new Reference().setReference("Patient/13275"));

        List<Observation.ObservationComponentComponent> observationComponentComponents2 = new ArrayList<>();
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org", "55423-8", "Number of steps"));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org", "55411-3", "Exercise duration"));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org", "55423-8", "Number of steps"));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org", "55411-3", "Exercise duration"));
        observationComponentComponents2.add(createObservationComponentComponent("http://loinc.org", "66267-6", "Time Period"));

        observation2.setComponent(observationComponentComponents2);

        entryComponent.setResource(observation2);
        entries.add(entryComponent);

        bundle.setEntry(entries);

        return bundle;
    }

    private Observation.ObservationComponentComponent createObservationComponentComponent(String system, String code, String display) {
        Observation.ObservationComponentComponent componentComponent = new Observation.ObservationComponentComponent();
        componentComponent.setCode(createCoding(system, code, display));
        return componentComponent;
    }

    private CodeableConcept createCoding(String system, String code, String display) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding().setSystem(system).setCode(code).setDisplay(display);
        return codeableConcept;
    }
}

