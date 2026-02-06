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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDynamicMarkerListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassDynamicMarkerCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassDynamicMarkerDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassDynamicMarkerService;

import java.util.List;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_DYNAMIC_MARKER_MANAGE, Permissions.TWIN_CLASS_DYNAMIC_MARKER_CREATE})
public class TwinClassDynamicMarkerCreateController extends ApiController {

    private final TwinClassDynamicMarkerService twinClassDynamicMarkerService;
    private final TwinClassDynamicMarkerCreateRestDTOReverseMapper twinClassDynamicMarkerCreateRestDTOReverseMapper;
    private final TwinClassDynamicMarkerDTOMapper twinClassDynamicMarkerDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "TwinClassDynamicMarkerCreateV1", summary = "Create new twin class dynamic marker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class dynamic marker data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = TwinClassDynamicMarkerListRsDTOv1.class)),}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_dynamic_marker/v1", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinClassDynamicMarkerCreateV1(
            @MapperContextBinding(roots = TwinClassDynamicMarkerDTOMapper.class, response = TwinClassDynamicMarkerListRsDTOv1.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassDynamicMarkerCreateRqDTOv1 request) {
        TwinClassDynamicMarkerListRsDTOv1 rs = new TwinClassDynamicMarkerListRsDTOv1();
        try {
            List<TwinClassDynamicMarkerEntity> twinClassDynamicMarkerEntityList = twinClassDynamicMarkerService.createTwinClassDynamicMarkerList(
                    twinClassDynamicMarkerCreateRestDTOReverseMapper.convertCollection(request.getDynamicMarkers(), mapperContext)
            );
            rs
                    .setDynamicMarkers(twinClassDynamicMarkerDTOMapper.convertCollection(twinClassDynamicMarkerEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
