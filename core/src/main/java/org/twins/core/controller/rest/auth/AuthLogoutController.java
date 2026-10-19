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
import org.twins.core.dto.rest.auth.AuthLogoutRqDTOv1;
import org.twins.core.dto.rest.auth.AuthLogoutRsDTOv1;
import org.twins.core.dto.rest.face.FaceViewRsDTOv1;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.mappers.rest.auth.ClientLogoutDataRestDTOReverseMapper;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Auth logout controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthLogoutController extends ApiController {
    private final IdentityProviderService identityProviderService;
    private final ClientLogoutDataRestDTOReverseMapper clientLogoutDataRestDTOReverseMapper;
    private final HttpRequestService httpRequestService;

    @ParametersApiUserHeaders
    @Operation(operationId = "authLogoutV1", summary = "Logout from identity provider, linked to current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout success  ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FaceViewRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/auth/logout/v1")
    public ResponseEntity<?> authLoginV1(@RequestBody AuthLogoutRqDTOv1 request) {
        AuthLogoutRsDTOv1 rs = new AuthLogoutRsDTOv1();
        try {
            ClientLogoutData clientLogoutData = clientLogoutDataRestDTOReverseMapper.convert(request.getAuthData());
            String authToken = httpRequestService.getAuthTokenFromRequest();
            if (clientLogoutData == null) {
                clientLogoutData = new ClientLogoutData();
            }
            clientLogoutData.put("authToken", authToken);
            identityProviderService.logout(clientLogoutData);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
