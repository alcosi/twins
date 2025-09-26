package org.twins.core.controller.rest.projection;

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
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.projection.ProjectionRestDTOMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.permission.Permissions;

import java.util.List;

@Tag(description = "", name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_EXCLUSION_MANAGE, Permissions.PROJECTION_EXCLUSION_CREATE})
public class ProjectionExclusionCreateController extends ApiController {
    private final ProjectionExclusionRestDTOMapper projectionExclusionRestDTOMapper;
    private final ProjectionExclusionCreateRestDTOReverseMapper projectionExclusionCreateRestDTOReverseMapper;
    private final ProjectionEsxclusionService projectionExclusionService;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionExclusionCreateV1", summary = "Create new projection exclusions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection exclusion data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = ProjectionExclusionCreateRsDTOv1.class)),}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/projection_exclusion/v1", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> projectionCreateV1(
            @MapperContextBinding(roots = ProjectionRestDTOMapper.class, response = ProjectionExclusionCreateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody ProjectionExclusionCreateRqDTOv1 request) {
        ProjectionExclusionCreateRsDTOv1 rs = new ProjectionExclusionCreateRsDTOv1();
        try {
            List<ProjectionEntity> projectionEntityList = projectionExclusionService.createProjectionExclusionList(projectionExclusionCreateRestDTOReverseMapper.convertCollection(request.getProjectionList()));
            rs
                    .setProjectionList(projectionExclusionRestDTOMapper.convertCollection(projectionEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
