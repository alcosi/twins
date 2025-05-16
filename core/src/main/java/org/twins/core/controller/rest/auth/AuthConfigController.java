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
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterDomainHeader;
import org.twins.core.domain.auth.IdentityProviderConfig;
import org.twins.core.dto.rest.auth.AuthConfigRsDTOv1;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

@Tag(description = "Auth config controller", name = ApiTag.AUTH)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthConfigController extends ApiController {
    private final AuthService authService;
    private final IdentityProviderService identityProviderService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParameterDomainHeader
    @Operation(operationId = "authConfigV1", summary = "Returns auth configuration for selected domain. Wit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login to ", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AuthConfigRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/auth/config/v1")
    public ResponseEntity<?> authConfigV1() {
        AuthConfigRsDTOv1 rs = new AuthConfigRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            IdentityProviderConfig providerServiceConfig = identityProviderService.getConfig();
            //todo convert to response
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
