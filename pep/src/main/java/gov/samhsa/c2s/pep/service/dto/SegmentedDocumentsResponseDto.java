package gov.samhsa.c2s.pep.service.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SegmentedDocumentsResponseDto {

    private List<SegmentedPatientDocument> documents = new ArrayList<>();
}
