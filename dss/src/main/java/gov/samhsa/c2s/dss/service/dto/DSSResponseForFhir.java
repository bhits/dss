package gov.samhsa.c2s.dss.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DSSResponseForFhir {
    @NotNull
    @JsonDeserialize(using = FhirStu3BundleDeserializer.class)
    @JsonSerialize(using = FhirStu3BundleSerializer.class)
    private Bundle fhirStu3Bundle;
}
