package gov.samhsa.c2s.pep.web;

import gov.samhsa.c2s.pep.service.PepService;
import gov.samhsa.c2s.pep.service.dto.AccessRequestDto;
import gov.samhsa.c2s.pep.service.dto.AccessResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/pep")
@Slf4j
public class PepRestController {

   private final PepService pepService;

    public PepRestController(PepService pepService) {
        this.pepService = pepService;
    }


    @RequestMapping(value = "/access", method = RequestMethod.POST)
    public AccessResponseDto access(@Valid @RequestBody AccessRequestDto accessRequest, Principal principal) {
        return pepService.accessDocument(accessRequest);
    }


}
