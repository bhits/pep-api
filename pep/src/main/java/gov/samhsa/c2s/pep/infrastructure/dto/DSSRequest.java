package gov.samhsa.c2s.pep.infrastructure.dto;


import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
@Builder
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
