package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ValidationRequestDto {

    @NotNull
    private byte[] document;
}
