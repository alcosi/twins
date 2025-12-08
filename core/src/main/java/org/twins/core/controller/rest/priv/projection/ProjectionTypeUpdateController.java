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
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeListRsDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeUpdateRqDTOv1;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.projection.ProjectionTypeService;
import org.twins.core.service.permission.Permissions;

import java.util.List;


@Tag(description = "", name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_MANAGE, Permissions.PROJECTION_CREATE})
public class ProjectionTypeUpdateController extends ApiController {
    private final ProjectionTypeUpdateDTOReverseMapper projectionTypeUpdateDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final ProjectionTypeService projectionTypeService;
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionTypeUpdateV1", summary = "Projection type update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection types updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionTypeListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/projection_type/v1")
    public ResponseEntity<?> projectionTypeUpdateV1(
            @MapperContextBinding(roots = ProjectionTypeRestDTOMapper.class, response = ProjectionTypeListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody ProjectionTypeUpdateRqDTOv1 request) {
        ProjectionTypeListRsDTOv1 rs = new ProjectionTypeListRsDTOv1();
        try {
            List<ProjectionTypeEntity> projectionTypeEntities = projectionTypeService.updateProjectionTypes(projectionTypeUpdateDTOReverseMapper.convertCollection(request.getProjectionTypes()));
            rs
                    .setProjectionTypes(projectionTypeRestDTOMapper.convertCollection(projectionTypeEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
