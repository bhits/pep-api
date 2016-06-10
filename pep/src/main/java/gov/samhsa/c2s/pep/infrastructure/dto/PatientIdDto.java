package gov.samhsa.c2s.pep.infrastructure.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class PatientIdDto {
    @NotBlank
    private String root;

    @NotBlank
    private String extension;
}
