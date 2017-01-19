package gov.samhsa.c2s.pep.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s.pep")
@Data
public class PepProperties {

    @NotNull
    @Valid
    private Iexhub iexhub;

    @NotNull
    @Valid
    private Patient patient;

    @NotEmpty
    private String authorNpiXpath;

    @Data
    public static class Iexhub {
        @NotEmpty
        private String url;

        @NotEmpty
        private String paramKey;

        @NotEmpty
        private String paramValueSuffix;
    }

    @Data
    public static class Patient {
        @NotEmpty
        private String documentName;
    }
}