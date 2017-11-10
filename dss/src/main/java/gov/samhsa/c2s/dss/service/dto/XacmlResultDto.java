package gov.samhsa.c2s.dss.service.dto;

import gov.samhsa.c2s.brms.domain.SubjectPurposeOfUse;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.dss.service.SupportedObligationId;
import lombok.Data;
import lombok.Singular;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@XmlRootElement(name = "xacmlResultDto")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class XacmlResultDto {

    @NotBlank
    private String pdpDecision;

    @NotNull
    @XmlElement(name = "purposeOfUse")
    private SubjectPurposeOfUse subjectPurposeOfUse;

    private String messageId;

    private String homeCommunityId;

    @NotNull
    @Singular
    @Valid
    @XmlElement(name = "pdpObligation")
    private List<ObligationDto> pdpObligations = new ArrayList<>();

    @NotNull
    @Valid
    private IdentifierDto patientIdentifier;

    public XacmlResult toXacmlResult() {
        SupportedObligationId.assertObligationIds(pdpObligations.stream().map(ObligationDto::getObligationId));
        final List<String> obligationValues = pdpObligations.stream()
                .filter(obligation -> SupportedObligationId.SHARE_SENSITIVITY_POLICY_CODE.equals(obligation.getObligationId()))
                .map(ObligationDto::getObligationValue)
                .collect(toList());
        final XacmlResult xacmlResult = new XacmlResult();
        xacmlResult.setPdpDecision(pdpDecision);
        xacmlResult.setPdpObligations(obligationValues);
        xacmlResult.setSubjectPurposeOfUse(subjectPurposeOfUse);
        xacmlResult.setPatientId(patientIdentifier.getValue());
        xacmlResult.setHomeCommunityId(patientIdentifier.getOid());
        xacmlResult.setMessageId(messageId);
        return xacmlResult;
    }
}
