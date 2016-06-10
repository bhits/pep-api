package gov.samhsa.c2s.pep.infrastructure.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class DSSResponse {

    @NotEmpty
    private byte[] segmentedDocument;

    private byte[] tryPolicyDocument;

    @NotBlank
    private String encoding;

    private boolean isCCDADocument;
}
