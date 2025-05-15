package org.twins.core.controller.rest.auth;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterDomainHeader;
import org.twins.core.dto.rest.auth.AuthLoginRqDTOv1;
import org.twins.core.dto.rest.auth.AuthLoginRsDTOv1;
import org.twins.core.featurer.identityprovider.token.ClientTokenData;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Auth login controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthLoginController extends ApiController {
    private final AuthService authService;
    private final IdentityProviderService identityProviderService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParameterDomainHeader
    @Operation(operationId = "authLoginV1", summary = "Returns an access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthLoginRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/login/v1")
    public ResponseEntity<?> authLoginV1(@RequestBody AuthLoginRqDTOv1 request) {
        AuthLoginRsDTOv1 rs = new AuthLoginRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            ClientTokenData clientTokenData = identityProviderService.login(request.getUsername(), request.getPassword());
            //todo convert to response
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
