package gov.samhsa.c2s.dss.infrastructure.valueset.dto;

import lombok.Data;

@Data
public class ConceptCodeAndCodeSystemOidDto {

    private String codedConceptCode;
    private String codeSystemOid;

    public ConceptCodeAndCodeSystemOidDto() {
    }

    public ConceptCodeAndCodeSystemOidDto(String codedConceptCode, String codeSystemOid) {
        this.codedConceptCode = codedConceptCode;
        this.codeSystemOid = codeSystemOid;
    }

}
