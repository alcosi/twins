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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterDomainHeader;
import org.twins.core.dto.rest.auth.AuthSignupByEmailConfirmRsDTOv1;
import org.twins.core.dto.rest.auth.AuthSignupByEmailRqDTOv1;
import org.twins.core.dto.rest.auth.AuthSignupByEmailRsDTOv1;
import org.twins.core.mappers.rest.auth.AuthSignupRestDTOReverseMapper;
import org.twins.core.mappers.rest.auth.ClientSideAuthDateRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Sign up controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthSignupByEmailController extends ApiController {
    private final AuthService authService;
    private final IdentityProviderService identityProviderService;
    private final ClientSideAuthDateRestDTOMapper clientSideAuthDateRestDTOMapper;
    private final AuthSignupRestDTOReverseMapper authSignUpRestDTOReverseMapper;


    @ParameterDomainHeader
    @Operation(operationId = "authSignupByEmailInitiateV1", summary = "Initiate signup by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SignUp to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthSignupByEmailRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/signup_by_email/initiate/v1")
    public ResponseEntity<?> authSignupByEmailInitiateV1(@RequestBody AuthSignupByEmailRqDTOv1 request) {
        AuthSignupByEmailRsDTOv1 rs = new AuthSignupByEmailRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            identityProviderService.signupByEmailInitiate(authSignUpRestDTOReverseMapper.convert(request));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParameterDomainHeader
    @Operation(operationId = "authSignupByEmailConfirmV1", summary = "Confirm email be token, which was sent to given email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SignUp to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthSignupByEmailConfirmRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/signup_by_email/confirm/v1")
    public ResponseEntity<?> authSignupByEmailConfirmV1(@RequestParam(required = true) String verificationToken) {
        AuthSignupByEmailConfirmRsDTOv1 rs = new AuthSignupByEmailConfirmRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            identityProviderService.signupByEmailConfirm(verificationToken);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
