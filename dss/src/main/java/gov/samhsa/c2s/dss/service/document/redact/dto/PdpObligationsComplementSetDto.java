package gov.samhsa.c2s.dss.service.document.redact.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdpObligationsComplementSetDto {
    /**
     * The set pdpObligationsComplementSet represents the complement of the set of pdp obligations present in a
     * {@link gov.samhsa.c2s.brms.domain.XacmlResult} object (which is based on the value set categories in a patient's consent).
     */
    private Set<String> pdpObligationsComplementSet = new HashSet<>();


    /**
     * This constructor takes a List of Strings representing the desired pdpObligationsComplementSet
     * and converts it to a Set of Strings before instantiating the new object with it.
     *
     * @param pdpObligationsComplementSetList - A list representation of pdpObligationsComplementSet
     */
    public PdpObligationsComplementSetDto(List<String> pdpObligationsComplementSetList) {
        this.pdpObligationsComplementSet = new HashSet<>(pdpObligationsComplementSetList);
    }

    /**
     * Gets the pdpObligationsComplementSet and converts it to an ArrayList before returning it.
     *
     * @return pdpObligationsComplementSet represented as an ArrayList
     */
    public List<String> getAsListPdpObligationsComplementSet() {
        return new ArrayList<>(this.pdpObligationsComplementSet);
    }
}
