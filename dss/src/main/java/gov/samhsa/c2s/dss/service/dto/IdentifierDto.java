package gov.samhsa.c2s.dss.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.Assert;

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
        Assert.hasText(oid, "oid must exist");
        Assert.hasText(value, "value must exist");
        return value + "^^^&" + oid + "&ISO";
    }
}
