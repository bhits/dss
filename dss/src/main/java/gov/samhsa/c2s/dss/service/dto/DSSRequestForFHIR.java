package gov.samhsa.c2s.dss.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import lombok.Data;
import org.hl7.fhir.dstu3.model.Bundle;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
public class DSSRequestForFHIR {

    @NotNull
    protected XacmlResult xacmlResult;
    protected Optional<Boolean> audited = Optional.empty();
    protected Optional<Boolean> auditFailureByPass = Optional.empty();
    protected Optional<Boolean> enableTryPolicyResponse = Optional.empty();
    @NotNull
    @JsonDeserialize(using = STU3FHIRBundleDeserializer.class)
    @JsonSerialize(using = STU3FHIRBundleSerializer.class)
    private Bundle stu3FHIRBundle;
}
