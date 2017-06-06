package gov.samhsa.c2s.pep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SegmentedPatientDocument {
    private String name;
    private byte[] document;
}
