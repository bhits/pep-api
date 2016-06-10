package gov.samhsa.c2s.pep.infrastructure.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class XacmlRequestDto {
    @NotBlank
    private String recipientNpi;

    @NotBlank
    private String intermediaryNpi;

    @NotNull
    private SubjectPurposeOfUse purposeOfUse;

    @NotNull
    private PatientIdDto patientId;
}
