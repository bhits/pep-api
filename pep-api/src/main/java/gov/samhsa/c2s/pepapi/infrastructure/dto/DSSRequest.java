package gov.samhsa.c2s.pepapi.infrastructure.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DSSRequest {

    @NotNull
    protected XacmlResult xacmlResult;

    protected boolean audited = false;

    protected boolean auditFailureByPass = false;

    protected boolean enableTryPolicyResponse = false;

    @NotEmpty
    private byte[] document;

    @NotBlank
    private String documentEncoding = "UTF-8";
}
