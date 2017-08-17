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
public class RuleExecutionResponseMarkerForRedactedEntriesTest {

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

    private RuleExecutionResponseMarkerForRedactedEntries sut;

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
        sut = new RuleExecutionResponseMarkerForRedactedEntries(documentAccessor);
    }

    @Test
    public void testExecute_By_Entry_Match() throws IOException, SimpleMarshallerException, XPathExpressionException {
        // Arrange
        String c32FileName = "MIE_SampleC32.xml";
        String factmodelXml = fileReader.readFile(TEST_PATH + FACTMODEL_PATH + c32FileName);
        String c32 = embeddedClinicalDocumentExtractor.extractClinicalDocumentFromFactModel(factmodelXml);
        String ruleExecutionContainerXml = fileReader.readFile(TEST_PATH + RULEEXECUTIONCONTAINER_PATH + c32FileName);
        RuleExecutionContainer ruleExecutionContainer = marshaller.unmarshalFromXml(RuleExecutionContainer.class, ruleExecutionContainerXml);
        Document c32Document = documentXmlConverter.loadDocument(c32);
        Document factModelDocument = documentXmlConverter.loadDocument(factmodelXml);
        FactModel factModel = marshaller.unmarshalFromXml(FactModel.class, factmodelXml);
        RedactionHandlerResult preRedactionResults = new RedactionHandlerResult();
        preRedactionResults.getRedactSectionCodesAndGeneratedEntryIds().add("d1e1406");

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
                factModelDocument, ruleExecutionContainer, preRedactionResults, pdpObligationsComplementSetDto,
                DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3);

        // Assert
        assertEquals(2, ruleExecutionContainer.getSize());
        assertEquals("REDACT", ruleExecutionContainer.getExecutionResponseList().get(0).getItemAction());
        assertEquals("REDACT", ruleExecutionContainer.getExecutionResponseList().get(1).getItemAction());
    }

    @Test
    public void testExecute_By_Section_Match() throws IOException, SimpleMarshallerException, XPathExpressionException {
        // Arrange
        String c32FileName = "MIE_SampleC32.xml";
        String factmodelXml = fileReader.readFile(TEST_PATH + FACTMODEL_PATH + c32FileName);
        String c32 = embeddedClinicalDocumentExtractor.extractClinicalDocumentFromFactModel(factmodelXml);
        String ruleExecutionContainerXml = fileReader.readFile(TEST_PATH + RULEEXECUTIONCONTAINER_PATH + c32FileName);
        RuleExecutionContainer ruleExecutionContainer = marshaller.unmarshalFromXml(RuleExecutionContainer.class, ruleExecutionContainerXml);
        Document c32Document = documentXmlConverter.loadDocument(c32);
        Document factModelDocument = documentXmlConverter.loadDocument(factmodelXml);
        FactModel factModel = marshaller.unmarshalFromXml(FactModel.class, factmodelXml);
        RedactionHandlerResult preRedactionResults = new RedactionHandlerResult();
        preRedactionResults.getRedactSectionCodesAndGeneratedEntryIds().add("10160-0");

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
                factModelDocument, ruleExecutionContainer, preRedactionResults, pdpObligationsComplementSetDto,
                DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3);

        // Assert
        assertEquals(2, ruleExecutionContainer.getSize());
        assertEquals("REDACT", ruleExecutionContainer.getExecutionResponseList().get(0).getItemAction());
        assertEquals("REDACT", ruleExecutionContainer.getExecutionResponseList().get(1).getItemAction());
    }
}
