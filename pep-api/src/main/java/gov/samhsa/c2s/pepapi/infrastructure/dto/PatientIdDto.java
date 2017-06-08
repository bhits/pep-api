package gov.samhsa.c2s.pepapi.infrastructure.dto;

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
    /**
     * The patient domain ID
     */
    @NotBlank
    private String root;
    /**
     * The patient MRN
     */
    @NotBlank
    private String extension;
}
