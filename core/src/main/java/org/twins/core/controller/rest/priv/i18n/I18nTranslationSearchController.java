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
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.domain.search.I18nTranslationSearch;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nTranslationSearchRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nTranslationRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.i18n.I18nTranslationSearchService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(description = "", name = ApiTag.I18N)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class I18nTranslationSearchController extends ApiController {
    private final I18nTranslationSearchService i18nTranslationSearchService;
    private final I18nTranslationRestDTOMapper i18nTranslationRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "i18nTranslationGetV1", summary = "View a list of all i18n translations for the i18n")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = I18nTranslationSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/i18n/{i18nId}/v1")
    public ResponseEntity<?> i18nTranslationViewV1(
            @MapperContextBinding(roots = I18nTranslationRestDTOMapper.class, response = I18nTranslationSearchRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.I18N_ID) @PathVariable UUID i18nId) {
        I18nTranslationSearchRsDTOv1 rs = new I18nTranslationSearchRsDTOv1();
        try {
            I18nTranslationSearch i18nTranslationSearch = new I18nTranslationSearch().setI18nIdList(Set.of(i18nId));

            List<I18nTranslationEntity> i18nTranslationsList = i18nTranslationSearchService.viewI18nTranslations(i18nTranslationSearch);

            rs
                    .setI18nTranslations(i18nTranslationRestDTOMapper.convertCollection(i18nTranslationsList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}