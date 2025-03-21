package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.domain.apiuser.*;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.DomainUserAddRqDTOv1;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUserAddController extends ApiController {
    private final DomainService domainService;
    private final AuthService authService;

    @ParameterChannelHeader
    @Operation(operationId = "domainUserAddV1", summary = "Add user to domain" +
            "If user is not exist it will be created.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/{domainId}/user/v1")
    public ResponseEntity<?> domainUserAddV1(
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId,
            @RequestBody DomainUserAddRqDTOv1 request) {
        Response rs = new Response();
        try {
            authService.getApiUser()
                    .setDomainResolver(new DomainResolverGivenId(domainId))
                    .setUserResolver(new UserResolverGivenId(request.userId))
                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified())
                    .setLocaleResolver(new LocaleResolverGivenOrSystemDefault(request.getLocale()))
                    .setCheckMembershipMode(false);
            domainService.addUser(
                    domainService.checkDomainId(domainId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS),
                    request.userId,
                    EntitySmartService.SaveMode.ifNotPresentCreate, true
            );
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
