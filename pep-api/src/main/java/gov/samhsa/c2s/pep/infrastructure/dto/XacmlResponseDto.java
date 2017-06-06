package gov.samhsa.c2s.pep.infrastructure.dto;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XacmlResponseDto {
    @NotBlank
    private String pdpDecision;

    @NotNull
    @Singular
    private List<String> pdpObligations = new ArrayList<>();
}
