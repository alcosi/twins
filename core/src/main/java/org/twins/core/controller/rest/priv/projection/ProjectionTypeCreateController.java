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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeCreateRqDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeListRsDTOv1;
import org.twins.core.mappers.rest.projection.ProjectionTypeCreateDTOReverseMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.projection.ProjectionTypeService;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_MANAGE, Permissions.PROJECTION_CREATE})
public class ProjectionTypeCreateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final ProjectionTypeCreateDTOReverseMapper projectionTypeCreateDTOReverseMapper;
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;
    private final ProjectionTypeService projectionTypeService;


    @ParametersApiUserHeaders
    @Operation(operationId = "projectionTypeCreateV1", summary = "Projection type create")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection types created", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionTypeListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/projection_type/v1")
    public ResponseEntity<?> projectionTypeCreateV1(
            @MapperContextBinding(roots = ProjectionTypeRestDTOMapper.class, response = ProjectionTypeListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody ProjectionTypeCreateRqDTOv1 request) {
        ProjectionTypeListRsDTOv1 rs = new ProjectionTypeListRsDTOv1();
        try {
            List<ProjectionTypeEntity> projectionTypeEntities = projectionTypeService.createProjectionTypes(projectionTypeCreateDTOReverseMapper.convertCollection(request.getProjectionTypes()));
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
