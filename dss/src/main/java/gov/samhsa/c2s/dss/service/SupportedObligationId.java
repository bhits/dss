package gov.samhsa.c2s.dss.service;

import gov.samhsa.c2s.dss.service.exception.UnsupportedObligationIdException;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public interface SupportedObligationId {
    String SHARE_SENSITIVITY_POLICY_CODE = "urn:samhsa:names:tc:consent2share:1.0:obligation:share-sensitivity-policy-code";

    static void assertObligationIds(Stream<String> obligationIdStream) {
        final Set<String> unsupportedObligationIds = obligationIdStream
                .filter(StringUtils::hasText)
                .filter(obligationId -> !SHARE_SENSITIVITY_POLICY_CODE.equals(obligationId))
                .collect(toSet());
        if (!unsupportedObligationIds.isEmpty()) {
            throw new UnsupportedObligationIdException("Contains unsupported obligationIds: " + unsupportedObligationIds.toString() + "; must not be other than: " + SupportedObligationId.SHARE_SENSITIVITY_POLICY_CODE);
        }
    }
}
