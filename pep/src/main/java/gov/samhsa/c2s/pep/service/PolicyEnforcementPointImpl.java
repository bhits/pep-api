package gov.samhsa.c2s.pep.service;

import gov.samhsa.c2s.pep.infrastructure.ContextHandlerService;
import gov.samhsa.c2s.pep.infrastructure.DssService;
import gov.samhsa.c2s.pep.infrastructure.dto.DSSRequest;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResponseDto;
import gov.samhsa.c2s.pep.infrastructure.dto.XacmlResult;
import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import gov.samhsa.c2s.pep.service.exception.AccessDeniedException;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Service
public class PolicyEnforcementPointImpl implements PolicyEnforcementPoint {

    private static final String PERMIT = "permit";

    @Autowired
    private ContextHandlerService contextHandler;

    @Autowired
    private DssService dssService;

    @Override
    public AccessResponseDto accessDocument(AccessRequestDto accessRequest) {
        val xacmlRequest = accessRequest.getXacmlRequest();
        // FIXME (BU): enable after context handler is implemented
        // val xacmlResponse = contextHandler.enforcePolicy(xacmlRequest);
        // FIXME (BU): remove the line below
        val xacmlResponse = XacmlResponseDto.builder()
                .pdpDecision(PERMIT)
                .pdpObligations(Arrays.asList("ETH", "GDIS", "HIV", "PSY", "SEX", "ALC", "COM", "ADD"))
                .build();
        val xacmlResult = XacmlResult.from(xacmlRequest, xacmlResponse);

        // throw AccessDeniedException if not permit
        Optional.of(xacmlResponse)
                .map(XacmlResponseDto::getPdpDecision)
                .filter(PERMIT::equalsIgnoreCase)
                .orElseThrow(AccessDeniedException::new);

        val dssRequest = DSSRequest.builder()
                .document(accessRequest.getDocument())
                .documentEncoding(accessRequest.getDocumentEncoding().orElse(StandardCharsets.UTF_8.name()))
                .xacmlResult(xacmlResult)
                .build();

        val dssResponse = dssService.segmentDocument(dssRequest);
        val accessResponse = AccessResponseDto.builder()
                .segmentedDocument(dssResponse.getSegmentedDocument())
                .segmentedDocumentEncoding(dssResponse.getEncoding())
                .build();
        return accessResponse;
    }
}
