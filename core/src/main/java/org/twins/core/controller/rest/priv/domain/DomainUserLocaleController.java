package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.domain.DomainUserNoRelationProjection;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.LocaleRsDTOv1;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.Locale;

@Tag(description = "Get data lists", name = ApiTag.LOCALE)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUserLocaleController extends ApiController {
    final AuthService authService;
    final DomainService domainService;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainUserLocaleUpdateV1", summary = "Update user locale in domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/locale/{localeName}/v1")
    public ResponseEntity<?> domainUserLocaleUpdateV1(
            @Parameter(example = DTOExamples.LOCALE) @PathVariable Locale localeName) {
        Response rs = new Response();
        try {
            domainService.updateLocaleByDomainUser(localeName);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "domainUserLocaleViewV1", summary = "View user locale in domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LocaleRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/locale/v1")
    public ResponseEntity<?> domainUserLocaleViewV1() {
        LocaleRsDTOv1 rs = new LocaleRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.setLocale(domainService.getDomainUserNoRelationProjection(apiUser.getDomainId(), apiUser.getUserId(), DomainUserNoRelationProjection.class).i18nLocaleId());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
