package gov.samhsa.c2s.pep.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gov.samhsa.c2s.pep.infrastructure.dto.SubjectPurposeOfUse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class DocumentsResponseDto {

    private ArrayList<PatientDocument> documents;

    public DocumentsResponseDto() {
        this.documents = new ArrayList<PatientDocument>();
    }

    public ArrayList<PatientDocument> getDocuments() {
        return this.documents;
    }

    public void setDocuments(ArrayList<PatientDocument> documents) {
        this.documents = documents;
    }
}
