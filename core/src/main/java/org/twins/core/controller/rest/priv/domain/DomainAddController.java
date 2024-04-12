package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserNoDomainHeaders;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.apiuser.LocaleResolverGivenOrSystemDefault;
import org.twins.core.domain.apiuser.UserResolverSystem;
import org.twins.core.dto.rest.domain.DomainAddRqDTOv1;
import org.twins.core.dto.rest.domain.DomainViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainAddController extends ApiController {
    final DomainService domainService;
    final AuthService authService;
    final UserResolverSystem userResolverSystem;
    final DomainAddRestDTOReverseMapper domainAddRestDTOReverseMapper;
    final DomainViewRestDTOMapper domainViewRestDTOMapper;

    @ParametersApiUserNoDomainHeaders
    @Operation(operationId = "domainAddV1", summary = "Add new domain.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/domain/v1", method = RequestMethod.POST)
    public ResponseEntity<?> domainAddV1(
            @RequestBody DomainAddRqDTOv1 request) {
        DomainViewRsDTOv1 rs = new DomainViewRsDTOv1();
        try {
            authService.getApiUser().setLocaleResolver(new LocaleResolverGivenOrSystemDefault(request.defaultLocale));
            DomainEntity domainEntity = domainService.addDomain(domainAddRestDTOReverseMapper.convert(request));
            rs.setDomain(domainViewRestDTOMapper.convert(domainEntity));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
