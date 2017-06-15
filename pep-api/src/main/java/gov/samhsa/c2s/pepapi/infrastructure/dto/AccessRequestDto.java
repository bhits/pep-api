package gov.samhsa.c2s.pepapi.infrastructure.dto;

import gov.samhsa.c2s.pepapi.infrastructure.dto.DSSRequest;
import gov.samhsa.c2s.pepapi.infrastructure.dto.XacmlRequestDto;
import gov.samhsa.c2s.pepapi.infrastructure.dto.XacmlResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequestDto {
    @NotNull
    private XacmlRequestDto xacmlRequest;

    @NotEmpty
    private byte[] document;

    @NotNull
    @UnwrapValidatedValue(false)
    private Optional<String> documentEncoding = Optional.empty();

    public DSSRequest toDSSRequest(XacmlResult xacmlResult) {
        return DSSRequest.builder()
                .document(getDocument())
                .documentEncoding(getDocumentEncoding().orElse(StandardCharsets.UTF_8.name()))
                .xacmlResult(xacmlResult.toBuilder().build())
                .build();
    }
}
