package gov.samhsa.c2s.dss.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.validation.FhirValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {

    @Bean
    public FhirContext fhirContext() {
        FhirContext fhirContext = FhirContext.forDstu3();
        return fhirContext;
    }

    @Bean
    public XmlParser fhirXmlParser() {
        return (XmlParser) fhirContext().newXmlParser();
    }

    @Bean
    public JsonParser fhirJsonParser() {
        return (JsonParser) fhirContext().newJsonParser();
    }

    @Bean
    public FhirValidator fhirValidator() {
        return fhirContext().newValidator();
    }
}
