package gov.samhsa.c2s.dss.infrastructure.valueset.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSetCategoryResponseDto {
    private String code;
    private String displayName;
    private String description;
    private boolean isFederal;
    private int displayOrder;
    private String system;
}
