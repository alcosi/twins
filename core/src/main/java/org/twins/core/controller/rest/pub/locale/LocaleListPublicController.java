package org.twins.core.controller.rest.pub.locale;

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
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParameterDomainHeader;
import org.twins.core.dto.rest.domain.LocaleListRsDTOv1;
import org.twins.core.mappers.rest.locale.LocaleRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

@Tag(description = "Get domain locale lists", name = ApiTag.LOCALE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class LocaleListPublicController extends ApiController {
    final AuthService authService;
    final LocaleRestDTOMapper localeRestDTOMapper;
    final DomainService domainService;

    @ParameterDomainHeader
    @Operation(operationId = "localeListPublicViewV1", summary = "Return list of locales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public list details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LocaleListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/public/locale/list/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> localeListPublicViewV1() {
        LocaleListRsDTOv1 rs = new LocaleListRsDTOv1();
        try {
            authService.getApiUser().setAnonymousWithDefaultLocale();
            rs
                    .setLocaleList(localeRestDTOMapper.convertCollection(domainService.getLocaleList()));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
