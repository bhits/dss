package gov.samhsa.c2s.dss.infrastructure.valueset;

import gov.samhsa.c2s.common.filereader.FileReader;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ConceptCodeAndCodeSystemOidDto;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetCategoryMapResponseDto;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetCategoryResponseDto;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class ValueSetServiceImplMock implements ValueSetService {
    private static final String VALUE_SET_MOCK_DATA_PATH = "MockValueSetData.csv";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private FileReader fileReader;
    private List<ConceptCode> conceptCodeList;

    public ValueSetServiceImplMock() {
    }

    public ValueSetServiceImplMock(FileReader fileReader) {
        this.fileReader = fileReader;
        try {
            init();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


    @Override
    public String toString() {
        return conceptCodeList.toString();
    }

    private void init() throws IOException {
        conceptCodeList = new LinkedList<>();
        String file = fileReader.readFile(VALUE_SET_MOCK_DATA_PATH);
        Scanner scanFile = new Scanner(file);
        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            Scanner scanLine = new Scanner(line);
            scanLine.useDelimiter(",");
            ConceptCode code = new ConceptCode();
            while (scanLine.hasNext()) {
                String next = scanLine.next();
                if ("304.31".equals(next)) {
                }
                code.setVariable(next);
            }
            conceptCodeList.add(code);
        }
    }

    private boolean isEqual(ConceptCode c1, String code, String codeSystem) {
        return c1.getCode().equals(code)
                && c1.getCodeSystem().equals(codeSystem);
    }

    @Override
    public List<ValueSetCategoryResponseDto> getAllValueSetCategories() {
        ValueSetCategoryResponseDto cat1 = new ValueSetCategoryResponseDto("ETH", "Drug use information", "Drug abuse or substance abuse is the use of mood-altering substances that interfere with or have a negative effect on a person’s life. These include negative effects on a person’s physical, psychological, social, emotional, occupational, and educational well-being. Drug abuse is characterized by dysfunction and negative consequences. Most drugs of abuse are mood altering (they change a person’s mood or feeling), and fall in three categories: stimulants, depressants, and hallucinogens.", true, 1, "http://hl7.org/fhir/v3/ActCode");
        ValueSetCategoryResponseDto cat2 = new ValueSetCategoryResponseDto("HIV", "HIV/AIDS information", "Human immunodeficiency virus (HIV) is a virus that weakens a person’s immune system by destroying important cells that fight disease and infection. HIV infection typically begins with flu-like symptoms followed by a long symptom-free period. HIV can be controlled with antiretroviral therapy. Untreated, HIV can advance to acquire immunodeficiency syndrome (AIDS), the most severe phase of HIV infection. People with AIDS have such badly damaged immune systems that they get an increasing number of severe illnesses, which can lead to death.", false, 4, "http://hl7.org/fhir/v3/ActCode");
        ValueSetCategoryResponseDto cat3 = new ValueSetCategoryResponseDto("PSY", "Mental health information", "Mental illness or a psychiatric disorder is a condition that affects a person’s thinking, feeling, or mood, and may affect his or her ability to relate to others and function well on a daily basis. Mental illnesses are medical conditions that often cause a diminished ability to cope with the ordinary demands of life. Like other medical disorders, mental illness ranges from mild to severe. There is a wide variety of treatments for mental illnesses.", false, 3, "http://hl7.org/fhir/v3/ActCode");
        ValueSetCategoryResponseDto cat4 = new ValueSetCategoryResponseDto("SEX", "Sexuality and reproductive health information", "Good sexual and reproductive health is a state of complete physical, mental, and social well-being in all matters relating to the reproductive system, at all stages of life. It implies that people are able to have a satisfying and safe sex life, the capacity to reproduce, and the freedom to decide if, when, and how often to do so. Similarly, sexual health is a state of physical, emotional, and social well-being in relation to sexuality. It is not simply the absence of disease, dysfunction, or infirmity.", false, 7, "http://hl7.org/fhir/v3/ActCode");
        ValueSetCategoryResponseDto cat5 = new ValueSetCategoryResponseDto("ALC", "Alcohol use and Alcoholism Information", "Alcohol abuse is the use of alcohol in such a way that it interferes with or has a negative effect on a person’s life. These include negative effects on a person’s physical, psychological, social, emotional, occupational, and educational well-being. Alcoholism or alcohol addiction is a primary, chronic, and disabling disorder that involves compulsion, loss of control, and continued use despite negative consequences. Genetic, psychosocial, and environmental factors influence its development and outcome.", true, 2, "http://hl7.org/fhir/v3/ActCode");
        ValueSetCategoryResponseDto cat6 = new ValueSetCategoryResponseDto("COM", "Communicable disease information", "Communicable diseases, also known as infectious diseases are illnesses that result from the infection, presence, and growth of organisms and microorganisms such as bacteria, viruses, fungi, and parasites. They can be spread, directly or indirectly, from one person to another.", false, 6, "http://hl7.org/fhir/v3/ActCode");

        List<ValueSetCategoryResponseDto> allValueSetCategoriesList = new ArrayList<>();

        allValueSetCategoriesList.add(cat1);
        allValueSetCategoriesList.add(cat2);
        allValueSetCategoriesList.add(cat3);
        allValueSetCategoriesList.add(cat4);
        allValueSetCategoriesList.add(cat5);
        allValueSetCategoriesList.add(cat6);

        return allValueSetCategoriesList;
    }

    @Override
    public List<ValueSetCategoryMapResponseDto> lookupValueSetCategories(@Valid @RequestBody List<ConceptCodeAndCodeSystemOidDto> conceptCodeAndCodeSystemOidDtos) {
        return conceptCodeAndCodeSystemOidDtos.stream()
                .map(dto -> {
                    final Optional<ConceptCode> any = conceptCodeList.stream()
                            .filter(cc -> dto.getCodedConceptCode().equals(cc.getCode()) && dto.getCodeSystemOid().equals(cc.getCodeSystem()))
                            .findAny();
                    return any.map(cc -> {
                        ValueSetCategoryMapResponseDto r = new ValueSetCategoryMapResponseDto();
                        r.setCodedConceptCode(dto.getCodedConceptCode());
                        r.setCodeSystemOid(dto.getCodeSystemOid());
                        Set<String> vs = new HashSet<>();
                        vs.add(cc.getValueSetCategory());
                        r.setValueSetCategoryCodes(vs);
                        return r;
                    }).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }
}
