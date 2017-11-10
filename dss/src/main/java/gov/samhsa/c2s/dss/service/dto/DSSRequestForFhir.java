package gov.samhsa.c2s.dss.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.hl7.fhir.dstu3.model.Bundle;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
public class DSSRequestForFhir {

    @NotNull
    @Valid
    protected XacmlResultDto xacmlResult;
    protected Optional<Boolean> audited = Optional.empty();
    protected Optional<Boolean> auditFailureByPass = Optional.empty();
    protected Optional<Boolean> enableTryPolicyResponse = Optional.empty();
    protected Optional<Boolean> enableRedact = Optional.empty();
    protected Optional<Boolean> enableBundleValidation = Optional.empty();

    @NotNull
    @JsonDeserialize(using = FhirStu3BundleDeserializer.class)
    @JsonSerialize(using = FhirStu3BundleSerializer.class)
    private Bundle fhirStu3Bundle;
}
