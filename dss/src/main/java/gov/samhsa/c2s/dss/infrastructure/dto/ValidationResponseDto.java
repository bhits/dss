package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

import java.util.List;

@Data
public class ValidationResponseDto {
    private String documentType;
    private boolean isDocumentValid;
    private DocumentValidationResultSummary validationResultSummary;
    private List<DocumentValidationResultDetail> validationResultDetails;
}
