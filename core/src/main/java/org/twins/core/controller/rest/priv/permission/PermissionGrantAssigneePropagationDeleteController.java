package org.twins.core.controller.rest.priv.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.permission.PermissionGrantAssigneePropagationService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Delete permission grant assignee propagation", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_DELETE)
public class PermissionGrantAssigneePropagationDeleteController extends ApiController {
    private final PermissionGrantAssigneePropagationService permissionGrantAssigneePropagationService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantAssigneePropagationDeleteV1", summary = "Delete permission grant assignee propagation by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/permission_grant/assignee_propagation/{permissionGrantAssigneePropagationId}/v1")
    public ResponseEntity<?> permissionGrantAssigneePropagationDeleteV1(
            @Parameter(example = DTOExamples.PERMISSION_GRANT_ASSIGNEE_PROPAGATION_ID) @PathVariable UUID permissionGrantAssigneePropagationId) {
        Response rs = new Response();
        try {
            permissionGrantAssigneePropagationService.deleteById(permissionGrantAssigneePropagationId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
