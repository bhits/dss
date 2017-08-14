package gov.samhsa.c2s.dss.service.document.redact.impl.documentlevel;

import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.filereader.FileReader;
import gov.samhsa.c2s.common.filereader.FileReaderImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerException;
import gov.samhsa.c2s.dss.config.DssProperties;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UnsupportedSectionHandlerTest {
    public static final String DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3 = "CCDA_R2_1_CCD_V3";
    public static final String TEST_PATH = "sampleC32/";
    public static final List<String> sectionWhiteList = Arrays.asList("11450-4", "48765-2", "10160-0", "30954-2");

    private FileReader fileReader;
    private DocumentAccessor documentAccessor;
    private DocumentXmlConverter documentXmlConverter;
    private DssProperties dssProperties;
    private UnsupportedSectionHandler sut;

    @Before
    public void setUp() throws Exception {
        fileReader = new FileReaderImpl();
        documentAccessor = new DocumentAccessorImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        dssProperties = new DssProperties();
        final DssProperties.Redact redact = new DssProperties.Redact();
        final Map<String, DssProperties.DocumentTypeDetail> documentTypeDetailMap = new HashMap<>();
        DssProperties.DocumentTypeDetail documentTypeDetail = new DssProperties.DocumentTypeDetail();
        documentTypeDetail.setSectionWhiteList(sectionWhiteList);
        documentTypeDetailMap.put(DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3, documentTypeDetail);
        redact.setDocumentTypes(documentTypeDetailMap);
        dssProperties.setRedact(redact);
        sut = new UnsupportedSectionHandler(documentAccessor, dssProperties);
    }

    @Test
    public void testExecute()
            throws IOException, SimpleMarshallerException,
            XPathExpressionException {
        // Arrange
        String c32FileName = "JohnHalamkaCCDDocument_C32.xml";
        String c32 = fileReader.readFile(TEST_PATH + c32FileName);
        Document c32Document = documentXmlConverter.loadDocument(c32);

        // Act
        final RedactionHandlerResult response = sut.execute(c32Document, DOCUMENT_TYPE_CCDA_R_2_1_CCD_V_3);

        // Assert
        assertEquals(12, response.getRedactNodeList().size());
    }
}
