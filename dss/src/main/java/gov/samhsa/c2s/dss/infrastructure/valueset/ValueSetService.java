package gov.samhsa.c2s.dss.infrastructure.valueset;


import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ConceptCodeAndCodeSystemOidDto;
import gov.samhsa.c2s.dss.infrastructure.valueset.dto.ValueSetQueryDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.List;

@FeignClient("pcm")
public interface ValueSetService {

    @RequestMapping(value = "/lookupService/valueSetCategories", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    List<ValueSetQueryDto> lookupValueSetCategories(@Valid @RequestBody List<ConceptCodeAndCodeSystemOidDto> conceptCodeAndCodeSystemOidDtos);
}
