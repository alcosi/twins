package org.twins.core.controller.rest.priv.i18n;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dto.rest.i18n.I18nTranslationListRsDTOv1;
import org.twins.core.dto.rest.i18n.I18nTranslationUpdateRqDTOv1;
import org.twins.core.mappers.rest.i18n.I18nTranslationRestDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nTranslationUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.i18n.I18nTranslationService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.I18N)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.I18N_MANAGE, Permissions.I18N_UPDATE})
public class I18nTranslationUpdateController extends ApiController {
    private final I18nTranslationUpdateDTOReverseMapper i18nTranslationUpdateDTOReverseMapper;
    private final I18nTranslationRestDTOMapper i18nTranslationRestDTOMapper;
    private final I18nTranslationService i18nTranslationService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "i18nTranslationUpdateV1", summary = "I18n translations for update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "I18n translation data updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = I18nTranslationListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/i18n_translation/v1")
    public ResponseEntity<?> i18nTranslationUpdateV1(
            @MapperContextBinding(roots = I18nTranslationRestDTOMapper.class, response = I18nTranslationListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody I18nTranslationUpdateRqDTOv1 request) {
        I18nTranslationListRsDTOv1 rs = new I18nTranslationListRsDTOv1();
        try {
            List<I18nTranslationEntity> updatedTranslations = i18nTranslationUpdateDTOReverseMapper.convertCollection(request.getTranslations());
            updatedTranslations = i18nTranslationService.updateTranslations(updatedTranslations);
            rs
                    .setTranslation(i18nTranslationRestDTOMapper.convertCollection(updatedTranslations, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
