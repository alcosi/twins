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
import org.twins.core.dto.rest.auth.AuthSignupRqDTOv1;
import org.twins.core.dto.rest.auth.AuthSignupRsDTOv1;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.mappers.rest.auth.AuthSignupRestDTOReverseMapper;
import org.twins.core.mappers.rest.auth.ClientSideAuthDateRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Sign up controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthSignupController extends ApiController {
    private final AuthService authService;
    private final IdentityProviderService identityProviderService;
    private final ClientSideAuthDateRestDTOMapper clientSideAuthDateRestDTOMapper;
    private final AuthSignupRestDTOReverseMapper authSignUpRestDTOReverseMapper;


    @ParameterDomainHeader
    @Operation(operationId = "authSignUpV1", summary = "Returns auth/refresh tokens by username/password and fingerprint (if required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SignUp to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthSignupRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/signup/v1")
    public ResponseEntity<?> authSignUpV1(@RequestBody AuthSignupRqDTOv1 request) {
        AuthSignupRsDTOv1 rs = new AuthSignupRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            ClientSideAuthData clientSideAuthData = identityProviderService.signup(authSignUpRestDTOReverseMapper.convert(request));
            rs.setAuthData(clientSideAuthDateRestDTOMapper.convert(clientSideAuthData));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
