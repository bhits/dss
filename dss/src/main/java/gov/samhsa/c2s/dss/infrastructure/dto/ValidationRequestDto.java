package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequestDto {

    @NotNull
    private byte[] document;

    private Optional<String> documentEncoding = Optional.empty();
}
