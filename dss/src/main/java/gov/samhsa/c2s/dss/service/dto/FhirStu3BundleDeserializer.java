package gov.samhsa.c2s.dss.service.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FhirStu3BundleDeserializer extends JsonDeserializer<Bundle> {
    @Autowired
    private ca.uhn.fhir.parser.JsonParser fhirJsonParser;

    @Override
    public Bundle deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final String jsonAsString = jsonParser.readValueAsTree().toString();
        final IBaseResource iBaseResource = fhirJsonParser.parseResource(jsonAsString);
        return (Bundle) iBaseResource;
    }
}
