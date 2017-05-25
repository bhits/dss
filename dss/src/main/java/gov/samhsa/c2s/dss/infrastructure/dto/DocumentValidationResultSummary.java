package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

import java.util.List;

@Data
public class DocumentValidationResultSummary {
    private String validationCriteria;
    private List<ValidationDiagnosticStatistics> diagnosticStatistics;
}