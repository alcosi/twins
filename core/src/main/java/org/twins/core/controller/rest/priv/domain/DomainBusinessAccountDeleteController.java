package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.DOMAIN_MANAGE)
public class DomainBusinessAccountDeleteController extends ApiController {
    private final DomainService domainService;
    private final AuthService authService;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainBusinessAccountDeleteV1", summary = "Delete businessAccount from domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BusinessAccount was deleted", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/domain/business_account/v1")
    public ResponseEntity<?> domainBusinessAccountDeleteV1() {
        Response rs = new Response();
        try {
            ApiUser apiUser = authService.getApiUser();

            domainService.deleteBusinessAccountFromDomain(
                    domainService.checkDomainId(apiUser.getDomain().getId(), EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS),
                    apiUser.getBusinessAccount().getId());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
