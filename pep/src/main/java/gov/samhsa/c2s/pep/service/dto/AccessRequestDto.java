package gov.samhsa.c2s.pep.service.dto;

import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
public class AccessRequestDto {
    @NotNull
    private XacmlRequestDto xacmlRequest;

    @NotEmpty
    private byte[] document;

    @NotNull
    @UnwrapValidatedValue(false)
    private Optional<String> documentEncoding = Optional.empty();
}
