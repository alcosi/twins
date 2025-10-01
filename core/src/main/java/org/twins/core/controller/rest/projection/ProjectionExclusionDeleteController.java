package org.twins.core.controller.rest.projection;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.projection.ProjectionExclusionDeleteRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionExclusionService;

@Tag(description = "", name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_EXCLUSION_MANAGE, Permissions.PROJECTION_EXCLUSION_DELETE})
public class ProjectionExclusionDeleteController extends ApiController {
    private final ProjectionExclusionService projectionExclusionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionExclusionDeleteV1", summary = "Delete projection exclusions by self id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection exclusions deleted successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/projection_exclusion/v1")
    public ResponseEntity<?> projectionExclusionDeleteV1(
            @RequestBody ProjectionExclusionDeleteRqDTOv1 projectionExclusionDeleteRqDTOv1) {
        Response rs = new Response();
        try {
            projectionExclusionService.deleteProjectionExclusions(projectionExclusionDeleteRqDTOv1.getProjectionExclusionIds());
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
