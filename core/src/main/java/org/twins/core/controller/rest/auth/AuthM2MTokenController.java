package org.twins.core.controller.rest.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
import org.twins.core.dto.rest.auth.AuthM2MLoginRqDTOv1;
import org.twins.core.dto.rest.auth.AuthM2MTokenRsDTOv1;
import org.twins.core.featurer.identityprovider.M2MAuthData;
import org.twins.core.mappers.rest.auth.AuthM2MLoginRestDTOReverseMapper;
import org.twins.core.mappers.rest.auth.ClientSideAuthDataRestDTOMapper;
import org.twins.core.mappers.rest.auth.CryptKeyRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Machine to machine auth", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthM2MTokenController extends ApiController {
    private final AuthService authService;
    private final IdentityProviderService identityProviderService;
    private final ClientSideAuthDataRestDTOMapper clientSideAuthDataRestDTOMapper;
    private final AuthM2MLoginRestDTOReverseMapper m2MLoginRestDTOReverseMapper;
    private final CryptKeyRestDTOMapper cryptKeyRestDTOMapper;

    @Deprecated
    @ParameterDomainHeader
    @Operation(operationId = "authM2MLoginV1", summary = "Returns auth data for machine-to-machine + act-as-user public key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthM2MTokenRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/m2m/login/v1")
    public ResponseEntity<?> authM2MLoginV1(@RequestBody AuthM2MLoginRqDTOv1 request, HttpServletResponse servletResponse) {
        AuthM2MTokenRsDTOv1 rs = new AuthM2MTokenRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            M2MAuthData m2MAuthData = identityProviderService.m2mAuth(m2MLoginRestDTOReverseMapper.convert(request));
            rs
                    .setAuthData(clientSideAuthDataRestDTOMapper.convert(m2MAuthData.getClientSideAuthData()))
                    .setActAsUserPublicKey(cryptKeyRestDTOMapper.convert(m2MAuthData.getActAsUserKey()));
            m2MAuthData.getClientSideAuthData().addCookiesToResponse(servletResponse);

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParameterDomainHeader
    @Operation(operationId = "authM2MTokenV1", summary = "Returns auth data for machine-to-machine + act-as-user public key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthM2MTokenRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/m2m/token/v1")
    public ResponseEntity<?> authM2MTokenV1(@RequestBody AuthM2MLoginRqDTOv1 request, HttpServletResponse servletResponse) {
        AuthM2MTokenRsDTOv1 rs = new AuthM2MTokenRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            M2MAuthData m2MAuthData = identityProviderService.m2mAuth(m2MLoginRestDTOReverseMapper.convert(request));
            rs
                    .setAuthData(clientSideAuthDataRestDTOMapper.convert(m2MAuthData.getClientSideAuthData()))
                    .setActAsUserPublicKey(cryptKeyRestDTOMapper.convert(m2MAuthData.getActAsUserKey()));
            m2MAuthData.getClientSideAuthData().addCookiesToResponse(servletResponse);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
