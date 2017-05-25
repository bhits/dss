package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

@Data
public class ValidationDiagnosticStatistics {
    private String diagnosticType;
    private int count;
}