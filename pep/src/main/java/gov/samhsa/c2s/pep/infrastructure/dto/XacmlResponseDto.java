package gov.samhsa.c2s.pep.infrastructure.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class XacmlResponseDto {
    @NotBlank
    private String pdpDecision;

    @NotNull
    private List<String> pdpObligations = new ArrayList<>();
}
