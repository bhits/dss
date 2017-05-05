package gov.samhsa.c2s.brms.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntryReference {

    private String entry;
    private String reference;
}
