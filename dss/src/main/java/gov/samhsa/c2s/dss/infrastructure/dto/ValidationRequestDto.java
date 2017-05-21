package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationRequestDto {

    @NotNull
    private byte[] document;
}
