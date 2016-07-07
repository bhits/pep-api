package gov.samhsa.c2s.pep.service.dto;

import gov.samhsa.c2s.pep.infrastructure.dto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDto {

    @NotNull
    private SubjectPurposeOfUse purposeOfUse;

    @NotNull
    private String mrn;

    @NotNull
    private String domain;
}
