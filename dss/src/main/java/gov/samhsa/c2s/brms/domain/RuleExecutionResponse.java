/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.c2s.brms.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlTransient;

/**
 * The Class RuleExecutionResponse.
 */
@Data
public class RuleExecutionResponse {

    /**
     * The Constant ITEM_ACTION_REDACT.
     */
    public static final String ITEM_ACTION_REDACT = "REDACT";

    /**
     * The Constant ITEM_ACTION_NO_ACTION.
     */
    public static final String ITEM_ACTION_NO_ACTION = "NO_ACTION";

    /**
     * The Implied conf section.
     */
    private Confidentiality ImpliedConfSection;

    /**
     * The Sensitivity.
     */
    private gov.samhsa.c2s.brms.domain.Sensitivity Sensitivity;

    /**
     * The Item action.
     */
    private String ItemAction = ITEM_ACTION_NO_ACTION;

    /**
     * The US privacy law.
     */
    private UsPrivacyLaw USPrivacyLaw;

    /**
     * The Document obligation policy.
     */
    private ObligationPolicyDocument DocumentObligationPolicy;

    /**
     * The Document refrain policy.
     */
    private RefrainPolicy DocumentRefrainPolicy;

    /**
     * The clinical fact.
     */
    @XmlTransient
    private ClinicalFact clinicalFact;

    /**
     * The code.
     */
    private String code;

    /**
     * The code system.
     */
    private String codeSystem;

    /**
     * The code system name.
     */
    private String codeSystemName;

    /**
     * The display name.
     */
    private String displayName;

    /**
     * The c32 section title.
     */
    private String c32SectionTitle;

    /**
     * The c32 section loinc code.
     */
    private String c32SectionLoincCode;

    /**
     * The observation id.
     */
    private String observationId;

    /**
     * The entry.
     */
    private String entry;

    /**
     * Sets the clinical fact.
     *
     * @param clinicalFact the new clinical fact
     */
    public void setClinicalFact(ClinicalFact clinicalFact) {
        this.code = clinicalFact.getCode();
        this.codeSystem = clinicalFact.getCodeSystem();
        this.c32SectionLoincCode = clinicalFact.getC32SectionLoincCode();
        this.displayName = clinicalFact.getDisplayName();
        this.observationId = clinicalFact.getObservationId();
        this.entry = clinicalFact.getEntry();
    }
}
