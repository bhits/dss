package gov.samhsa.c2s.dss.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SchemaBaseValidator;
import ca.uhn.fhir.validation.schematron.SchematronBaseValidator;
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
        FhirContext fhirContext =  fhirContext();
        FhirValidator fhirValidator = fhirContext().newValidator();

        // Create some modules and register them
        IValidatorModule module1 = new SchemaBaseValidator(fhirContext);
        fhirValidator.registerValidatorModule(module1);
        IValidatorModule module2 = new SchematronBaseValidator(fhirContext);
        fhirValidator.registerValidatorModule(module2);

        return fhirValidator;
    }
}
