package org.twins.core.controller.rest.priv.usergroup;

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
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.usergroup.UserGroupInvolveAssigneeService;

import java.util.UUID;

@Tag(description = "Delete user group by assignee propagation", name = ApiTag.USER_GROUP)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_GROUP_INVOLVE_ASSIGNEE_MANAGE, Permissions.USER_GROUP_INVOLVE_ASSIGNEE_DELETE})
public class UserGroupInvolveAssigneeDeleteController extends ApiController {
    private final UserGroupInvolveAssigneeService userGroupInvolveAssigneeService;

    @ParametersApiUserHeaders
    @Operation(operationId = "userGroupInvolveAssigneeDeleteV1", summary = "Delete user group by assignee propagation by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/user_group/involve_assignee/{userGroupInvolveAssigneeId}/v1")
    public ResponseEntity<?> userGroupInvolveAssigneeDeleteV1(
            @Parameter(example = DTOExamples.USER_GROUP_INVOLVE_ASSIGNEE_ID) @PathVariable UUID userGroupInvolveAssigneeId) {
        Response rs = new Response();
        try {
            userGroupInvolveAssigneeService.deleteById(userGroupInvolveAssigneeId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
