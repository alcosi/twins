package org.twins.core.controller.rest.priv.twinclass;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassCreateController extends ApiController {
    final AuthService authService;
    final UserService userService;
    final TwinClassService twinClassService;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final TwinClassCreateRestDTOReverseMapper twinClassCreateRestDTOReverseMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassCreateV1", summary = "Create new twin class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinClassCreateV1(
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.showClassMode, defaultValue = TwinClassBaseRestDTOMapper.ClassMode._SHORT) TwinClassBaseRestDTOMapper.ClassMode showClassMode,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode,
            @RequestParam(name = RestRequestParam.showClassHeadMode, defaultValue = TwinClassRestDTOMapper.HeadTwinMode._HIDE) TwinClassRestDTOMapper.HeadTwinMode showClassHeadMode,
            @RequestParam(name = RestRequestParam.showClassStatusMode, defaultValue = TwinClassRestDTOMapper.StatusMode._HIDE) TwinClassRestDTOMapper.StatusMode showClassStatusMode,
            @RequestParam(name = RestRequestParam.showClassMarkerMode, defaultValue = TwinClassRestDTOMapper.MarkerMode._HIDE) TwinClassRestDTOMapper.MarkerMode showClassMarkerMode,
            @RequestParam(name = RestRequestParam.showClassTagMode, defaultValue = TwinClassRestDTOMapper.TagMode._HIDE) TwinClassRestDTOMapper.TagMode showClassTagMode,
            @RequestParam(name = RestRequestParam.showLinkMode, defaultValue = LinkRestDTOMapper.Mode._HIDE) LinkRestDTOMapper.Mode showLinkMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._HIDE) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestBody TwinClassCreateRqDTOv1 request) {
        TwinClassCreateRsDTOv1 rs = new TwinClassCreateRsDTOv1();
        try {
            TwinClassEntity twinClassEntity = twinClassCreateRestDTOReverseMapper.convert(request);
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showClassMode)
                    .setMode(showClassHeadMode)
                    .setMode(showClassFieldMode)
                    .setMode(showClassStatusMode)
                    .setMode(showClassMarkerMode)
                    .setMode(showClassTagMode)
                    .setMode(showLinkMode)
                    .setMode(showStatusMode);
            twinClassEntity = twinClassService.createInDomainClass(twinClassEntity, request.getName(), request.getDescription());
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
