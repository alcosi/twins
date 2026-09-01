package org.twins.core.controller.rest.priv.projection;

import io.swagger.v3.oas.annotations.Operation;
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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupDeleteRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.projection.ProjectionTypeGroupService;

@Tag(name = ApiTag.PROJECTION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.PROJECTION_DELETE)
public class ProjectionTypeGroupDeleteController extends ApiController {
    private final ProjectionTypeGroupService projectionTypeGroupService;

    @ParametersApiUserHeaders
    @Operation(operationId = "projectionTypeGroupDeleteV1", summary = "Projection type group delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projection type groups deleted"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/projection_type_group/delete/v1")
    public ResponseEntity<?> projectionTypeGroupDeleteV1(
            @RequestBody ProjectionTypeGroupDeleteRqDTOv1 request) {
        Response rs = new Response();
        try {
            projectionTypeGroupService.deleteSafe(request.getProjectionTypeGroupIdList());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
