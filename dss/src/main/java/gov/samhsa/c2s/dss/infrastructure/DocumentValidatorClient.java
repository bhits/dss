package gov.samhsa.c2s.dss.infrastructure;

import gov.samhsa.c2s.dss.infrastructure.dto.ValidationRequestDto;
import gov.samhsa.c2s.dss.infrastructure.dto.ValidationResponseDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@FeignClient("document-validator")
// Get configured context-path
@RequestMapping(value = "${c2s.dss.document-validator.context-path:/}")
public interface DocumentValidatorClient {

    @RequestMapping(value = "/documentValidation", method = RequestMethod.POST)
    ValidationResponseDto validateClinicalDocument(@Valid @RequestBody ValidationRequestDto requestDto);
}
