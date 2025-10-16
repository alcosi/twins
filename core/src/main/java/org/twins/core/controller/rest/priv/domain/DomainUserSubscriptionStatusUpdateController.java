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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.permission.Permissions;

/**
 * Controller to update subscription status for current or specific user in a domain.
 */
@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUserSubscriptionStatusUpdateController extends ApiController {
    private final AuthService authService;
    private final DomainUserService domainUserService;

    public enum SubscriptionAction {init, stop}

    /*
     * Shortcut endpoint for current authenticated user (domain derived from auth context)
     */
    @ParametersApiUserHeaders
    @ProtectedBy(Permissions.DOMAIN_USER_UPDATE)
    @Operation(operationId = "domainCurrentUserSubscriptionStatusUpdateV1", summary = "Update subscription status for current user in current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/user/subscription/v1")
    public ResponseEntity<?> updateCurrentUserSubscriptionStatusV1(
            @Parameter(description = "Subscription action", example = "stop") @RequestParam("subscriptionAction") SubscriptionAction subscriptionAction) {
        Response rs = new Response();
        try {
            boolean enable = subscriptionAction == SubscriptionAction.init;
            domainUserService.updateDomainUserSubscriptionStatus(authService.getApiUser().getDomainId(), authService.getApiUser().getUserId(), enable);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
