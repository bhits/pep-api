package gov.samhsa.c2s.pep.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DSSResponse {

    @NotEmpty
    private byte[] segmentedDocument;

    private byte[] tryPolicyDocument;

    @NotBlank
    private String encoding;

    private boolean isCCDADocument;
}
