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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGroupedListRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGroupWithGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionWithGroupRestDTOMapper;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserPermissionListController extends ApiController {
    final PermissionWithGroupRestDTOMapper permissionWithGroupRestDTOMapper;
    final PermissionRestDTOMapper permissionRestDTOMapper;
    final PermissionGroupWithGroupRestDTOMapper permissionGroupWithGroupRestDTOMapper;
    final PermissionService permissionService;
    final UserService userService;

    @ParametersApiUserHeaders
    @Operation(operationId = "userPermissionListV1", summary = "Returns permission list for selected user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/{userId}/permission/v1", method = RequestMethod.GET)
    public ResponseEntity<?> userPermissionListV1(
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @RequestParam(name = RestRequestParam.showPermissionMode, defaultValue = PermissionRestDTOMapper.Mode._DETAILED) PermissionRestDTOMapper.Mode showPermissionMode,
            @RequestParam(name = RestRequestParam.showPermissionGroupMode, defaultValue = PermissionGroupRestDTOMapper.Mode._DETAILED) PermissionGroupRestDTOMapper.Mode showPermissionGroupMode) {
        PermissionListRsDTOv1 rs = new PermissionListRsDTOv1();
        try {
            rs.permissionList = permissionWithGroupRestDTOMapper.convertList(
                    permissionService.findPermissionsForUser(userService.checkId(userId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS)).collectPermissions(),
                    new MapperContext()
                            .setMode(showPermissionMode)
                            .setMode(showPermissionGroupMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "userPermissionGroupedListV1", summary = "Returns grouped permission list for selected user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionGroupedListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/{userId}/permission_group/v1", method = RequestMethod.GET)
    public ResponseEntity<?> userPermissionGroupedListV1(
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @RequestParam(name = RestRequestParam.showPermissionMode, defaultValue = PermissionRestDTOMapper.Mode._DETAILED) PermissionRestDTOMapper.Mode showPermissionMode,
            @RequestParam(name = RestRequestParam.showPermissionGroupMode, defaultValue = PermissionGroupRestDTOMapper.Mode._DETAILED) PermissionGroupRestDTOMapper.Mode showPermissionGroupMode) {
        PermissionGroupedListRsDTOv1 rs = new PermissionGroupedListRsDTOv1();
        try {
            rs.permissionGroups = permissionGroupWithGroupRestDTOMapper.convertList(
                    permissionService.findPermissionsForUser(userService.checkId(userId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS)).collectPermissionGroups(),
                    new MapperContext()
                            .setMode(showPermissionMode)
                            .setMode(showPermissionGroupMode));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
