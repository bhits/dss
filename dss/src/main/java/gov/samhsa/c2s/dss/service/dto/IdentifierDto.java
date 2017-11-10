package gov.samhsa.c2s.dss.service.dto;

import gov.samhsa.c2s.common.util.FullIdentifierBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdentifierDto {
    @NotBlank
    private String oid;
    @NotBlank
    private String value;

    public static IdentifierDto of(String oid, String value) {
        return builder().oid(oid).value(value).build();
    }

    public String getFullIdentifier() {
        return FullIdentifierBuilder.buildFullIdentifier(value, oid);
    }
}
