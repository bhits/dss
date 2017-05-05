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
public class DSSResponseForFHIR {
    @NotNull
    @JsonDeserialize(using = STU3FHIRBundleDeserializer.class)
    @JsonSerialize(using = STU3FHIRBundleSerializer.class)
    private Bundle stu3FHIRBundle;
}
