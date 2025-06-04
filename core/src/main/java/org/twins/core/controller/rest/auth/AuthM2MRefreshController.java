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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.auth.AuthM2MRefreshRsDTOv1;
import org.twins.core.dto.rest.auth.AuthRefreshRqDTOv1;
import org.twins.core.featurer.identityprovider.M2MAuthData;
import org.twins.core.mappers.rest.auth.ClientSideAuthDateRestDTOMapper;
import org.twins.core.mappers.rest.auth.CryptKeyRestDTOMapper;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthM2MRefreshController extends ApiController {
    private final IdentityProviderService identityProviderService;
    private final ClientSideAuthDateRestDTOMapper clientSideAuthDateRestDTOMapper;
    private final CryptKeyRestDTOMapper cryptKeyRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "authM2MRefreshV1", summary = "Refresh M2M auth_token by refresh_token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthM2MRefreshRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/m2m/refresh/v1")
    public ResponseEntity<?> authM2MRefreshV1(@RequestBody AuthRefreshRqDTOv1 request) {
        AuthM2MRefreshRsDTOv1 rs = new AuthM2MRefreshRsDTOv1();
        try {
            M2MAuthData m2MAuthData = identityProviderService.refreshM2M(request.getRefreshToken());
            rs
                    .setAuthData(clientSideAuthDateRestDTOMapper.convert(m2MAuthData.getClientSideAuthData()))
                    .setActAsUserPublicKey(cryptKeyRestDTOMapper.convert(m2MAuthData.getActAsUserKey()));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
