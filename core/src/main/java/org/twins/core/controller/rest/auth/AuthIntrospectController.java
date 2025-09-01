package org.twins.core.controller.rest.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.dto.rest.auth.AuthIntrospectRsDTOv1;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Auth token introspect controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class AuthIntrospectController extends ApiController {
    //todo - do we need @ProtectedBy for this endpoint?
    private final IdentityProviderService identityProviderService;
    private final HttpRequestService httpRequestService;

    @Operation(operationId = "authIntrospectV1", summary = "Return auth token metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "metadata returned",
                    content = @Content(
                            schema = @Schema(implementation = AuthIntrospectRsDTOv1.class))),
            @ApiResponse(responseCode = "401", description = "Access denied")})
    @GetMapping("/private/auth/introspect/v1")

    public ResponseEntity<?> introspectV1() {
        AuthIntrospectRsDTOv1 rs = new AuthIntrospectRsDTOv1();
        try {
            String authToken = httpRequestService.getAuthTokenFromRequest();
            TokenMetaData meta = identityProviderService.resolveAuthTokenMetaData(authToken);
            rs.setClientId(meta.getUserId().toString());
            //todo - check if it can be collected from the introspect endpoint or any other endpoint should be used rs.setTokenExpiryDate(meta.getExpiresAt());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
