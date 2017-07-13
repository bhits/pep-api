package gov.samhsa.c2s.pepapi.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XacmlRequestDto {
    @NotBlank
    private String recipientNpi;

    @NotBlank
    private String intermediaryNpi;

    @NotNull
    private SubjectPurposeOfUse purposeOfUse;

    @Valid
    @NotNull
    private PatientIdDto patientId;
}
