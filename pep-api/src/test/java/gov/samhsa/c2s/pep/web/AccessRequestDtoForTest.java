package gov.samhsa.c2s.pep.web;

import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequestDtoForTest {
    @NotNull
    private XacmlRequestDto xacmlRequest;

    @NotEmpty
    private byte[] document;

    @NotNull
    private String documentEncoding;
}
