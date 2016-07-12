package gov.samhsa.c2s.pep.service.dto;

import java.util.ArrayList;

public class SegmentedDocumentsResponseDto {

    private ArrayList<SegmentedPatientDocument> documents;

    public SegmentedDocumentsResponseDto() {
        this.documents = new ArrayList<SegmentedPatientDocument>();
    }

    public ArrayList<SegmentedPatientDocument> getDocuments() {
        return this.documents;
    }

    public void setDocuments(ArrayList<SegmentedPatientDocument> documents) {
        this.documents = documents;
    }
}
