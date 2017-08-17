package gov.samhsa.c2s.dss.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Configuration
@ConfigurationProperties(prefix = "c2s.dss")
@Data
@Slf4j
public class DssProperties {

    @NotNull
    @Valid
    private DocumentSegmentationImpl documentSegmentationImpl;

    @NotNull
    @Valid
    private Redact redact;

    @PostConstruct
    public void print() {
        if (log.isInfoEnabled()) {
            log.info(Stream.of("Redaction Configuration: ", this.toString()).collect(joining()));
        }
    }

    @Data
    public static class DocumentSegmentationImpl {
        @NotNull
        private boolean defaultIsAudited;

        @NotNull
        private boolean defaultIsAuditFailureByPass;
    }

    @Data
    public static class Redact {
        @NotEmpty
        private Map<String, DocumentTypeDetail> documentTypes = new HashMap<>();
    }

    @Data
    public static class DocumentTypeDetail {
        @NotEmpty
        private List<String> requiredSections = new ArrayList<>();
        @NotEmpty
        private List<String> sectionWhiteList = new ArrayList<>();
    }
}