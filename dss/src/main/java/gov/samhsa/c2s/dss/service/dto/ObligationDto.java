package gov.samhsa.c2s.dss.service.dto;

import gov.samhsa.c2s.common.validator.constraint.In;
import gov.samhsa.c2s.dss.service.SupportedObligationId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ObligationDto {
    @NotBlank
    @In(SupportedObligationId.SHARE_SENSITIVITY_POLICY_CODE)
    private String obligationId;
    @NotBlank
    private String obligationValue;
}
