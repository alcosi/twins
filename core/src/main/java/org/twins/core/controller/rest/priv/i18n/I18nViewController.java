package org.twins.core.controller.rest.priv.i18n;

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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nViewRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.I18N)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.I18N_VIEW)
public class I18nViewController extends ApiController {
    private final I18nService i18nService;
    private final I18nRestDTOMapper i18nRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "i18nViewV1", summary = "View a list of all i18n translations for the i18n")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = I18nViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/i18n/{i18nId}/v1")
    public ResponseEntity<?> i18nViewV1(
            @MapperContextBinding(roots = I18nRestDTOMapper.class, response = I18nViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.I18N_ID) @PathVariable UUID i18nId) {
        I18nViewRsDTOv1 rs = new I18nViewRsDTOv1();
        try {
            I18nEntity i18nEntity = i18nService.findEntitySafe(i18nId);
            rs
                    .setI18n(i18nRestDTOMapper.convert(i18nEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
