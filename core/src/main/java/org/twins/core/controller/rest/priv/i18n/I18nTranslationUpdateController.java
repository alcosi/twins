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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nTranslationDTOv1;
import org.twins.core.dto.rest.i18n.I18nTranslationSaveRsDTOv1;
import org.twins.core.dto.rest.i18n.I18nTranslationUpdateRqDTOv1;
import org.twins.core.mappers.rest.i18n.I18nTranslationRestDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nTranslationUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.i18n.I18nTranslationService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = ApiTag.I18N)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class I18nTranslationUpdateController extends ApiController {
    private final I18nTranslationUpdateDTOReverseMapper i18nTranslationUpdateDTOReverseMapper;
    private final I18nTranslationService i18nTranslationService;
    private final I18nTranslationRestDTOMapper i18nTranslationRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "i18nTranslationUpdateV1", summary = "I18n translation for update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "I18n translation data updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = I18nTranslationSaveRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/i18n/{i18nId}/v1")
    public ResponseEntity<?> i18nTranslationUpdateV1(
            @MapperContextBinding(roots = I18nTranslationRestDTOMapper.class, response = I18nTranslationSaveRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.I18N_ID) @PathVariable UUID i18nId,
            @RequestBody I18nTranslationUpdateRqDTOv1 request) {

        I18nTranslationSaveRsDTOv1 rs = new I18nTranslationSaveRsDTOv1();

        try {
            List<I18nTranslationEntity> i18nTranslations = i18nTranslationUpdateDTOReverseMapper.convert(request.getI18nTranslations());
            i18nTranslations = i18nTranslationService.updateI18nTranslations(i18nId, i18nTranslations);

            List<I18nTranslationDTOv1> translationDTOs = i18nTranslations.stream()
                    .map(entity -> {
                        I18nTranslationDTOv1 dto = new I18nTranslationDTOv1();
                        try {
                            i18nTranslationRestDTOMapper.map(entity, dto, mapperContext);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to map entity to DTO", e);
                        }
                        return dto;
                    })
                    .toList();

            rs
                    .setI18nTranslations(translationDTOs)
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
