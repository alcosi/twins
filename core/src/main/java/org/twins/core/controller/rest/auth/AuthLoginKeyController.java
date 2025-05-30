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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterDomainHeader;
import org.twins.core.domain.auth.CryptKey;
import org.twins.core.dto.rest.auth.AuthLoginKeyRsDTOv1;
import org.twins.core.mappers.rest.auth.LoginKeyRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Auth login public key controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthLoginKeyController extends ApiController {
    private final AuthService authService;
    private final IdentityProviderService identityProviderService;
    private final LoginKeyRestDTOMapper loginKeyRestDTOMapper;

    @ParameterDomainHeader
    @Operation(operationId = "authLoginKeyV2", summary = "Get public key to encrypt password during login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthLoginKeyRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/auth/login_key/v1")
    public ResponseEntity<?> authLoginKeyV2() {
        AuthLoginKeyRsDTOv1 rs = new AuthLoginKeyRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            CryptKey.LoginPublicKey clientSideAuthData = identityProviderService.getPublicKeyForPasswordCrypt();
            rs.setPublicKey(loginKeyRestDTOMapper.convert(clientSideAuthData));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
