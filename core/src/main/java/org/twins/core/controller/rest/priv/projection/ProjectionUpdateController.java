package org.twins.core.controller.rest.priv.projection;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dto.rest.projection.ProjectionUpdateRqDTOv1;
import org.twins.core.dto.rest.projection.ProjectionUpdateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.projection.ProjectionRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionService;

import java.util.List;

@Tag(description = "", name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_MANAGE, Permissions.PROJECTION_UPDATE})
public class ProjectionUpdateController extends ApiController {
    private final ProjectionService projectionService;
    private final ProjectionUpdateRestDTOReverseMapper projectionUpdateRestDTOReverseMapper;
    private final ProjectionRestDTOMapper projectionRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;


    @ParametersApiUserHeaders
    @Operation(operationId = "projectionUpdateV1", summary = "Update projections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/projection/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> projectionUpdateV1(
            @MapperContextBinding(roots = ProjectionRestDTOMapper.class, response = ProjectionUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody ProjectionUpdateRqDTOv1 request) {
        ProjectionUpdateRsDTOv1 rs = new ProjectionUpdateRsDTOv1();
        try {
            List<ProjectionEntity> projectionEntityList = projectionService.updateProjectionList(projectionUpdateRestDTOReverseMapper.convertCollection(request.getProjectionList()));
            rs
                    .setProjectionList(projectionRestDTOMapper.convertCollection(projectionEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
