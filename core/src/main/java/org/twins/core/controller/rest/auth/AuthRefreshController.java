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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.auth.AuthRefreshRqDTOv1;
import org.twins.core.dto.rest.auth.AuthRefreshRqDTOv2;
import org.twins.core.dto.rest.auth.AuthRefreshRsDTOv1;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.mappers.rest.auth.ClientSideAuthDataRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Auth login controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthRefreshController extends ApiController {
    private final AuthService authService;
    private final IdentityProviderService identityProviderService;
    private final ClientSideAuthDataRestDTOMapper clientSideAuthDataRestDTOMapper;

    @ParameterDomainHeader
    @Operation(operationId = "authRefreshV1", summary = "Refresh auth_token by refresh_token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthRefreshRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/refresh/v1")
    public ResponseEntity<?> authRefreshV1(@RequestBody AuthRefreshRqDTOv1 request, HttpServletResponse servletResponse) {
        AuthRefreshRsDTOv1 rs = new AuthRefreshRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            ClientSideAuthData clientSideAuthData = identityProviderService.refresh(request.getRefreshToken());
            rs.setAuthData(clientSideAuthDataRestDTOMapper.convert(clientSideAuthData));
            clientSideAuthData.addCookiesToResponse(servletResponse);

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParameterDomainHeader
    @Operation(operationId = "authRefreshV2", summary = "Refresh auth_token by refresh_token and fingerprint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthRefreshRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/refresh/v2")
    public ResponseEntity<?> authRefreshV2(@RequestBody AuthRefreshRqDTOv2 request, HttpServletResponse servletResponse) {
        AuthRefreshRsDTOv1 rs = new AuthRefreshRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            ClientSideAuthData clientSideAuthData = identityProviderService.refresh(request.getRefreshToken(), request.getFingerprint());
            rs.setAuthData(clientSideAuthDataRestDTOMapper.convert(clientSideAuthData));
            clientSideAuthData.addCookiesToResponse(servletResponse);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
