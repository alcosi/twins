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
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupListRsDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.projection.ProjectionTypeGroupRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeGroupUpdateDTOReverseMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionTypeGroupService;

import java.util.List;

@Tag(name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.PROJECTION_UPDATE)
public class ProjectionTypeGroupUpdateController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final ProjectionTypeGroupUpdateDTOReverseMapper projectionTypeGroupUpdateDTOReverseMapper;
    private final ProjectionTypeGroupRestDTOMapper projectionTypeGroupRestDTOMapper;
    private final ProjectionTypeGroupService projectionTypeGroupService;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionTypeGroupUpdateV1", summary = "Projection type group update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection type groups updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ProjectionTypeGroupListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/projection_type_group/v1")
    public ResponseEntity<?> projectionTypeGroupUpdateV1(
            @MapperContextBinding(roots = ProjectionTypeGroupRestDTOMapper.class, response = ProjectionTypeGroupListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody ProjectionTypeGroupUpdateRqDTOv1 request) {
        ProjectionTypeGroupListRsDTOv1 rs = new ProjectionTypeGroupListRsDTOv1();
        try {
            List<ProjectionTypeGroupEntity> entities = projectionTypeGroupService.updateProjectionTypeGroups(projectionTypeGroupUpdateDTOReverseMapper.convertCollection(request.getProjectionTypeGroups()));
            rs
                    .setProjectionTypeGroups(projectionTypeGroupRestDTOMapper.convertCollection(entities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
