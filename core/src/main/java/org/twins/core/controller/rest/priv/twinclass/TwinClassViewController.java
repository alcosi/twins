package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassViewController extends ApiController {
    final AuthService authService;
    final TwinClassService twinClassService;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;


    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassViewV1", summary = "Returns twin class by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/{twinClassId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassViewV1(
            @Parameter(name = "lazyRelation", in = ParameterIn.QUERY) @RequestParam(defaultValue = "true") boolean lazyRelation,
            @Parameter(name = "twinClassId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassHeadMode, defaultValue = TwinClassRestDTOMapper.HeadTwinMode._SHOW) TwinClassRestDTOMapper.HeadTwinMode showClassHeadMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode) {
        TwinClassRsDTOv1 rs = new TwinClassRsDTOv1();
        try {
            MapperContext mapperContext = new MapperContext().setLazyRelations(lazyRelation);
            rs.setTwinClass(
                    twinClassRestDTOMapper.convert(
                            twinClassService.findEntity(twinClassId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows), mapperContext
                                    .setMode(showClassMode)
                                    .setMode(showClassHeadMode)
                                    .setMode(showClassFieldMode)
                                    .setMode(showLinkMode)));
            rs.setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassViewByKeyV1", summary = "Returns twin class by key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class_by_key/{twinClassKey}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassViewByKeyV1(
            @Parameter(name = "lazyRelation", in = ParameterIn.QUERY) @RequestParam(defaultValue = "true") boolean lazyRelation,
            @Parameter(name = "twinClassKey", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_CLASS_KEY) @PathVariable String twinClassKey,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassHeadMode, defaultValue = TwinClassRestDTOMapper.HeadTwinMode._SHOW) TwinClassRestDTOMapper.HeadTwinMode showClassHeadMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode) {
        TwinClassRsDTOv1 rs = new TwinClassRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            MapperContext mapperContext = new MapperContext().setLazyRelations(lazyRelation);
            rs.setTwinClass(
                    twinClassRestDTOMapper.convert(
                            twinClassService.findTwinClassByKey(apiUser, twinClassKey), mapperContext
                                    .setMode(showClassMode)
                                    .setMode(showClassHeadMode)
                                    .setMode(showClassFieldMode)
                                    .setMode(showLinkMode)));
            rs.setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
