package gov.samhsa.c2s.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.filereader.FileReader;
import gov.samhsa.c2s.common.filereader.FileReaderImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerException;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.dss.infrastructure.valueset.ValueSetService;
import gov.samhsa.c2s.dss.infrastructure.valueset.ValueSetServiceImplMock;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetCategoryResponseDto;
import gov.samhsa.c2s.dss.service.document.EmbeddedClinicalDocumentExtractor;
import gov.samhsa.c2s.dss.service.document.EmbeddedClinicalDocumentExtractorImpl;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.dto.PdpObligationsComplementSetDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DocumentCleanupForDocumentationOfNoServiceEventTest {

    public static final String DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3 = "CCDA_R2_1_CCD_V3";
    public static final String TEST_PATH = "sampleC32-redactionHandlers/";
    public static final String FACTMODEL_PATH = "factmodel/";
    public static final String RULEEXECUTIONCONTAINER_PATH = "ruleexecutioncontainer/";

    private FileReader fileReader;
    private SimpleMarshaller marshaller;
    private DocumentAccessor documentAccessor;
    private DocumentXmlConverter documentXmlConverter;
    private EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor;

    private static ValueSetService valueSetService;

    private DocumentCleanupForDocumentationOfNoServiceEvent sut;

    @Before
    public void setUp() throws Exception {
        fileReader = new FileReaderImpl();
        marshaller = new SimpleMarshallerImpl();
        documentAccessor = new DocumentAccessorImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        embeddedClinicalDocumentExtractor = new EmbeddedClinicalDocumentExtractorImpl();
        ReflectionTestUtils.setField(embeddedClinicalDocumentExtractor, "documentXmlConverter", documentXmlConverter);
        ReflectionTestUtils.setField(embeddedClinicalDocumentExtractor, "documentAccessor", documentAccessor);
        valueSetService = new ValueSetServiceImplMock(fileReader);
        sut = new DocumentCleanupForDocumentationOfNoServiceEvent(documentAccessor);
    }

    @Test
    public void testExecute_DocumentationOf_Has_ServiceEvent() throws IOException, SimpleMarshallerException, XPathExpressionException {
        // Arrange
        String c32FileName = "c32.xml";
        String factmodelXml = fileReader.readFile(TEST_PATH + FACTMODEL_PATH + c32FileName);
        String c32 = embeddedClinicalDocumentExtractor.extractClinicalDocumentFromFactModel(factmodelXml);
        String ruleExecutionContainerXml = fileReader.readFile(TEST_PATH + RULEEXECUTIONCONTAINER_PATH + c32FileName);
        RuleExecutionContainer ruleExecutionContainer = marshaller.unmarshalFromXml(RuleExecutionContainer.class, ruleExecutionContainerXml);
        Document c32Document = documentXmlConverter.loadDocument(c32);
        Document factModelDocument = documentXmlConverter.loadDocument(factmodelXml);
        FactModel factModel = marshaller.unmarshalFromXml(FactModel.class, factmodelXml);
        RedactionHandlerResult preRedactionResults = new RedactionHandlerResult();

        Set<ValueSetCategoryResponseDto> allValueSetCategoryDtosSet = new HashSet<>(valueSetService.getAllValueSetCategories());
        Set<String> xacmlPdpObligations = new HashSet<>(factModel.getXacmlResult().getPdpObligations());

        Set<String> pdpObligationsComplementSet = new HashSet<>();

        // Calculate the set difference (i.e. complement set)
        pdpObligationsComplementSet.addAll(allValueSetCategoryDtosSet.stream()
                .map(ValueSetCategoryResponseDto::getCode)
                .filter(valSetCatCode -> !xacmlPdpObligations.contains(valSetCatCode))
                .collect(Collectors.toList()));

        PdpObligationsComplementSetDto pdpObligationsComplementSetDto = new PdpObligationsComplementSetDto(pdpObligationsComplementSet);

        // Act
        sut.execute(c32Document, factModel.getXacmlResult(), factModel,
                factModelDocument, ruleExecutionContainer, preRedactionResults, pdpObligationsComplementSetDto, DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3);

        // Assert
        assertEquals(1, documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").getLength());
        assertEquals(3, documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").item(0).getChildNodes().getLength());
        assertEquals("serviceEvent", documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").item(0).getChildNodes().item(1).getNodeName());
    }

    @Test
    public void testExecute_DocumentationOf_Does_Not_Have_ServiceEvent() throws IOException, SimpleMarshallerException, XPathExpressionException {
        // Arrange
        String c32FileName = "c32-emptyDocumentationOf.xml";
        String factmodelXml = fileReader.readFile(TEST_PATH + FACTMODEL_PATH + c32FileName);
        String c32 = embeddedClinicalDocumentExtractor.extractClinicalDocumentFromFactModel(factmodelXml);
        String ruleExecutionContainerXml = fileReader.readFile(TEST_PATH + RULEEXECUTIONCONTAINER_PATH + c32FileName);
        RuleExecutionContainer ruleExecutionContainer = marshaller.unmarshalFromXml(RuleExecutionContainer.class, ruleExecutionContainerXml);
        Document c32Document = documentXmlConverter.loadDocument(c32);
        Document factModelDocument = documentXmlConverter.loadDocument(factmodelXml);
        FactModel factModel = marshaller.unmarshalFromXml(FactModel.class, factmodelXml);
        RedactionHandlerResult preRedactionResults = new RedactionHandlerResult();

        Set<ValueSetCategoryResponseDto> allValueSetCategoryDtosSet = new HashSet<>(valueSetService.getAllValueSetCategories());
        Set<String> xacmlPdpObligations = new HashSet<>(factModel.getXacmlResult().getPdpObligations());

        Set<String> pdpObligationsComplementSet = new HashSet<>();

        // Calculate the set difference (i.e. complement set)
        pdpObligationsComplementSet.addAll(allValueSetCategoryDtosSet.stream()
                .map(ValueSetCategoryResponseDto::getCode)
                .filter(valSetCatCode -> !xacmlPdpObligations.contains(valSetCatCode))
                .collect(Collectors.toList()));

        PdpObligationsComplementSetDto pdpObligationsComplementSetDto = new PdpObligationsComplementSetDto(pdpObligationsComplementSet);

        // Act
        sut.execute(c32Document, factModel.getXacmlResult(), factModel,
                factModelDocument, ruleExecutionContainer, preRedactionResults, pdpObligationsComplementSetDto, DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3);

        // Assert
        assertEquals(0, documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").getLength());
    }
}
