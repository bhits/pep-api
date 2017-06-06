package gov.samhsa.c2s.pep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDto {

    @NotNull
    private String purposeOfUse;

    @NotNull
    private String mrn;

    @NotNull
    private String domain;
}
