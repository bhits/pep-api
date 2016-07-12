package gov.samhsa.c2s.pep.service.dto;

/**
 * Created by tomson.ngassa on 7/11/2016.
 */
public class SegmentedPatientDocument {
    private String name;
    private byte[] document;

    public SegmentedPatientDocument() {}

    public SegmentedPatientDocument(String name, byte[] document) {
        this.name = name;
        this.document = document;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }
}
