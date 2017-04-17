package gov.samhsa.c2s.dss.infrastructure.valueset.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSetCategoryMapResponseDto {
    private String codedConceptCode;
    private String codeSystemOid;
    private Set<String> valueSetCategoryCodes;
}
