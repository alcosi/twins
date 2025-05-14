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
import org.twins.core.service.permission.PermissionGrantUserService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Delete permission grant user", name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.PERMISSION_GRANT_USER_DELETE)
public class PermissionGrantUserDeleteController extends ApiController {
    private final PermissionGrantUserService permissionGrantUserService;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissionGrantUserDeleteV1", summary = "Delete permission grant user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission grant user was deleted successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/permission_grant/user/{permissionGrantUserId}/v1")
    public ResponseEntity<?> permissionGrantUserDeleteV1(
            @Parameter(example = DTOExamples.PERMISSION_GRANT_USER_ID) @PathVariable UUID permissionGrantUserId) {
        Response rs = new Response();
        try {
            permissionGrantUserService.deletePermissionGrantUser(permissionGrantUserId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
