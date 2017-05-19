package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

import java.util.List;

@Data
public class ValidationResponseDto {
    //TODO: Remove
    private DocumentValidationSummary validationSummary;
    private List<DocumentValidationResult> validationDetails;

    private String documentType;
    private boolean isDocumentValid;
    private DocumentValidationResultSummary validationResultSummary;
    private List<DocumentValidationResultDetail> validationResultDetails;
}
