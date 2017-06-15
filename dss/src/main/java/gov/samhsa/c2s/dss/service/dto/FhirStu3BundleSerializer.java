package gov.samhsa.c2s.dss.service.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hl7.fhir.dstu3.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FhirStu3BundleSerializer extends JsonSerializer<Bundle> {
    @Autowired
    private ca.uhn.fhir.parser.JsonParser fhirJsonParser;

    @Override
    public void serialize(Bundle bundle, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        final String json = fhirJsonParser.encodeResourceToString(bundle);
        jsonGenerator.writeRawValue(json);
    }
}
