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
import org.twins.core.domain.apiuser.DomainResolverGivenId;
import org.twins.core.domain.apiuser.UserResolverGivenId;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUserDeleteController extends ApiController {
    final DomainService domainService;
    final AuthService authService;
    @ParameterChannelHeader
    @Operation(operationId = "domainUserDeleteV1", summary = "Delete user from domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/domain/{domainId}/user/{userId}/v1")
    public ResponseEntity<?> domainUserDeleteV1(
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId) {
        Response rs = new Response();
        try {
            authService.getApiUser()
                    .setDomainResolver(new DomainResolverGivenId(domainId))
                    .setUserResolver(new UserResolverGivenId(userId));
            domainService.deleteUser(
                    domainService.checkDomainId(domainId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS),
                    userId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
