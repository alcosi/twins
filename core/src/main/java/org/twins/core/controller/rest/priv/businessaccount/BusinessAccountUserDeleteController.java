package org.twins.core.controller.rest.priv.businessaccount;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.apiuser.BusinessAccountResolverGivenId;
import org.twins.core.domain.apiuser.UserResolverGivenId;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountUserService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.BUSINESS_ACCOUNT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.BUSINESS_ACCOUNT_MANAGE)
public class BusinessAccountUserDeleteController extends ApiController {
    private final BusinessAccountUserService businessAccountUserService;
    private final AuthService authService;

    @Value("${api.unsecured.enable}")
    private boolean apiUnsecuredEnabled;

    @Deprecated
    @ParameterChannelHeader
    @Operation(operationId = "businessAccountUserDeleteV1", summary = "Delete user from businessAccount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/business_account/{businessAccountId}/user/{userId}/v1")
    public ResponseEntity<?> businessAccountUserDeleteV1(
            @Parameter(example = DTOExamples.BUSINESS_ACCOUNT_ID) @PathVariable UUID businessAccountId,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId) {
        Response rs = new Response();
        try {
            if (!apiUnsecuredEnabled)
                throw new ServiceException(ErrorCodeCommon.FORBIDDEN);
            authService.getApiUser()
                    .setBusinessAccountResolver(new BusinessAccountResolverGivenId(businessAccountId))
                    .setUserResolver(new UserResolverGivenId(userId));
            businessAccountUserService.deleteUser(
                    businessAccountUserService.checkId(businessAccountId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS),
                    userId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
