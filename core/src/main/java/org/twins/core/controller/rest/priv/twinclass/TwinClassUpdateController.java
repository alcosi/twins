package org.twins.core.controller.rest.priv.twinclass;

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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassSaveRestDTOReverseMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassUpdateController extends ApiController {
    final AuthService authService;
    final TwinClassService twinClassService;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    final TwinClassSaveRestDTOReverseMapper twinClassSaveRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassUpdateV1", summary = "Update twin class by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_class/{twinClassId}/v1")
    public ResponseEntity<?> twinClassUpdateV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassStatusMode, defaultValue = TwinClassRestDTOMapper.StatusMode._HIDE) TwinClassRestDTOMapper.StatusMode showClassStatusMode,
            @RequestParam(name = RestRequestParam.showClassMarkerMode, defaultValue = TwinClassRestDTOMapper.MarkerMode._HIDE) TwinClassRestDTOMapper.MarkerMode showClassMarkerMode,
            @RequestParam(name = RestRequestParam.showClassTagMode, defaultValue = TwinClassRestDTOMapper.TagMode._HIDE) TwinClassRestDTOMapper.TagMode showClassTagMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._HIDE) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showHeadClassMode, defaultValue = TwinClassRestDTOMapper.HeadClassMode._HIDE) TwinClassRestDTOMapper.HeadClassMode showHeadClassMode,
            @RequestParam(name = RestRequestParam.showExtendsClassMode, defaultValue = TwinClassRestDTOMapper.ExtendsClassMode._HIDE) TwinClassRestDTOMapper.ExtendsClassMode showExtendsClassMode,
            @RequestBody TwinClassUpdateRqDTOv1 request) {
        TwinClassRsDTOv1 rs = new TwinClassRsDTOv1();
        try {
            TwinClassEntity twinClassEntity = twinClassSaveRestDTOReverseMapper.convert(request)
                    .setId(twinClassId);
            twinClassService.updateTwinClass(twinClassEntity);
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showClassMode)
                    .setMode(showClassFieldMode)
                    .setMode(showClassStatusMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showLinkMode)
                    .setMode(showStatusMode)
                    .setMode(showExtendsClassMode)
                    .setMode(showHeadClassMode);
            rs
                    .setTwinClass(twinClassRestDTOMapper.convert(twinClassEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
