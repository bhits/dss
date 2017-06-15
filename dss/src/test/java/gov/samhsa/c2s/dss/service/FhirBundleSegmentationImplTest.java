package gov.samhsa.c2s.dss.service;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.brms.domain.*;
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
import gov.samhsa.c2s.dss.service.dto.DSSResponse;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import gov.samhsa.c2s.dss.service.metadata.AdditionalMetadataGeneratorForSegmentedClinicalDocumentImpl;
import gov.samhsa.c2s.dss.service.metadata.MetadataGeneratorImpl;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Meta;
import org.junit.Before;
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
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

public class FhirBundleSegmentationImplTest {

    private static final String FHIR_SEARCHSET_TYPE = "searchset";
    private static final String FHIR_REFERENCE = "reference";
    private static final String FHIR_SUBJECT = "subject";

    private static final String FHIR_CONFIDENTIALITY_CODE_V = "V";
    private static final String FHIR_CONFIDENTIALITY_CODE_R = "R";
    private static final String FHIR_CONFIDENTIALITY_CODE_N = "N";
    private static final String FHIR_SYSTEM_CONFIDENTIALITY = "http://hl7.org/fhir/v3/Confidentiality";

    @Before
    public void setUp() throws
            Exception {
    }

    @Test
    void unit_test_bundleConfidentialities(){
    }

    private Bundle createFhirBundle(){
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
//        bundle.setTotal(2);
        bundle.setMeta(new Meta().setLastUpdated(new Date()));

        List<Bundle.BundleEntryComponent> entries =  new ArrayList<>();

        Bundle.BundleEntryComponent entryComponent =  new Bundle.BundleEntryComponent();
        entryComponent.setFullUrl("https://fhirtest.uhn.ca/baseDstu2/Observation/13280");

        bundle.setEntry(entries);

        return bundle;
    }
}
