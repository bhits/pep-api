package gov.samhsa.c2s.pep.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientIdDto {
    @NotBlank
    private String root;

    @NotBlank
    private String extension;
}
