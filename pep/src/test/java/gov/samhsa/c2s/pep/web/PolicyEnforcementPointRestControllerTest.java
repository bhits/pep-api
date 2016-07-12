package gov.samhsa.c2s.pep.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.samhsa.c2s.pep.infrastructure.dto.PatientIdDto;
import gov.samhsa.c2s.pep.infrastructure.dto.SubjectPurposeOfUse;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlRequestDto;
import gov.samhsa.c2s.pep.service.PolicyEnforcementPoint;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import gov.samhsa.c2s.pep.service.exception.DocumentNotFoundException;
import lombok.val;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static gov.samhsa.mhc.common.unit.matcher.ArgumentMatchers.matching;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class PolicyEnforcementPointRestControllerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MockMvc mvc;
    private ObjectMapper objectMapper;

    @Mock
    private PolicyEnforcementPoint policyEnforcementPoint;

    @InjectMocks
    private PolicyEnforcementPointRestController sut;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(this.sut).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void access() throws Exception {
        // Arrange
        val recipientNpi = "recipientNpi";
        val intermediaryNpi = "intermediaryNpi";
        val purposeOfUse = SubjectPurposeOfUse.HEALTHCARE_TREATMENT;
        val extension = "extension";
        val root = "root";
        val patientId = PatientIdDto.builder().extension(extension).root(root).build();
        val xacmlRequest = XacmlRequestDto.builder().intermediaryNpi(intermediaryNpi).recipientNpi(recipientNpi).patientId(patientId).purposeOfUse(purposeOfUse).build();
        val document = "document";
        val documentEncoding = StandardCharsets.UTF_8;
        val documentBytes = document.getBytes(StandardCharsets.UTF_8);
        val documentEncodingString = documentEncoding.name();
        val request = AccessRequestDtoForTest.builder()
                .xacmlRequest(xacmlRequest)
                .document(documentBytes)
                .documentEncoding(documentEncodingString)
                .build();
        val segmentedDocument = "segmentedDocument";
        val segmentedDocumentBytes = segmentedDocument.getBytes(documentEncoding);
        final String segmentedDocumentBytesEncodedString = Base64.getEncoder().encodeToString(segmentedDocumentBytes);
        val response = AccessResponseDto.builder()
                .segmentedDocument(segmentedDocumentBytes)
                .segmentedDocumentEncoding(documentEncodingString)
                .build();
        when(policyEnforcementPoint.accessDocument(argThat(matching(
                req -> req.getXacmlRequest().equals(xacmlRequest) &&
                        document.equals(new String(req.getDocument(), documentEncoding)) &&
                        documentEncodingString.equals(req.getDocumentEncoding().get())
        )))).thenReturn(response);

        // Act and Assert
        mvc.perform(post("/access")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segmentedDocument", is(segmentedDocumentBytesEncodedString)))
                .andExpect(jsonPath("$.segmentedDocumentEncoding", is(documentEncodingString)));
        verify(policyEnforcementPoint, times(1)).accessDocument(argThat(matching(
                req -> req.getXacmlRequest().equals(xacmlRequest) &&
                        document.equals(new String(req.getDocument(), documentEncoding)) &&
                        documentEncodingString.equals(req.getDocumentEncoding().get())
        )));
    }

    @Test
    public void access_Throws_DocumentNotFoundException() throws Exception {
        // Arrange
        val recipientNpi = "recipientNpi";
        val intermediaryNpi = "intermediaryNpi";
        val purposeOfUse = SubjectPurposeOfUse.HEALTHCARE_TREATMENT;
        val extension = "extension";
        val root = "root";
        val patientId = PatientIdDto.builder().extension(extension).root(root).build();
        val xacmlRequest = XacmlRequestDto.builder().intermediaryNpi(intermediaryNpi).recipientNpi(recipientNpi).patientId(patientId).purposeOfUse(purposeOfUse).build();
        val document = "document";
        val documentEncoding = StandardCharsets.UTF_8;
        val documentBytes = document.getBytes(StandardCharsets.UTF_8);
        val documentEncodingString = documentEncoding.name();
        val request = AccessRequestDtoForTest.builder()
                .xacmlRequest(xacmlRequest)
                .document(documentBytes)
                .documentEncoding(documentEncodingString)
                .build();
        when(policyEnforcementPoint.accessDocument(argThat(matching(
                req -> req.getXacmlRequest().equals(xacmlRequest) &&
                        document.equals(new String(req.getDocument(), documentEncoding)) &&
                        documentEncodingString.equals(req.getDocumentEncoding().get())
        )))).thenThrow(DocumentNotFoundException.class);

        // Act and Assert
        mvc.perform(post("/access")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNotFound());
        verify(policyEnforcementPoint, times(1)).accessDocument(argThat(matching(
                req -> req.getXacmlRequest().equals(xacmlRequest) &&
                        document.equals(new String(req.getDocument(), documentEncoding)) &&
                        documentEncodingString.equals(req.getDocumentEncoding().get())
        )));
    }
}