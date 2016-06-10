package gov.samhsa.c2s.pep.service.dto;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Builder
public class AccessResponseDto {
    @NotEmpty
    private byte[] segmentedDocument;

    @NotBlank
    private String segmentedDocumentEncoding;
}
