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

//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentsResponseDto {

    private ArrayList<String> documents;

    public ArrayList<String> getDocuments() {
        return this.documents;
    }

    public void setDocuments(ArrayList<String> documents) {
        this.documents = documents;
    }
}
