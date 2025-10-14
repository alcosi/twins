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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.projection.ProjectionDeleteRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionService;

@Tag(description = "", name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.PROJECTION_MANAGE, Permissions.PROJECTION_DELETE})
public class ProjectionDeleteController extends ApiController {
    private final ProjectionService projectionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionDeleteV1", summary = "Delete projections by self id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projections deleted successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/projection/v1")
    public ResponseEntity<?> projectionDeleteV1(
            @RequestBody ProjectionDeleteRqDTOv1 projectionDeleteRqDTOv1) {
        Response rs = new Response();
        try {
            projectionService.deleteProjections(projectionDeleteRqDTOv1.getProjectionIds());
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
