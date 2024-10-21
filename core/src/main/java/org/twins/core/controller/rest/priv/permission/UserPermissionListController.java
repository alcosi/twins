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
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionGroupedListRsDTOv1;
import org.twins.core.dto.rest.permission.PermissionListRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionGroupWithGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapperV2;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(name = ApiTag.PERMISSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserPermissionListController extends ApiController {
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;
    private final PermissionGroupWithGroupRestDTOMapper permissionGroupWithGroupRestDTOMapper;
    private final PermissionService permissionService;
    private final UserService userService;

    @ParametersApiUserHeaders
    @Operation(operationId = "userPermissionListV1", summary = "Returns permission list for selected user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = PermissionListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/user/{userId}/permission/v1")
    public ResponseEntity<?> userPermissionListV1(
            @MapperContextBinding(roots = PermissionRestDTOMapperV2.class, response = PermissionListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId) {
        PermissionListRsDTOv1 rs = new PermissionListRsDTOv1();
        try {
            rs.setPermissions(permissionRestDTOMapperV2.convertCollection(
                    permissionService.findPermissionsForUser(userService.checkId(userId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS)).collectPermissions(), mapperContext));
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
            @MapperContextBinding(roots = PermissionGroupWithGroupRestDTOMapper.class, response = PermissionGroupedListRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId) {
        PermissionGroupedListRsDTOv1 rs = new PermissionGroupedListRsDTOv1();
        try {
            rs.permissionGroups = permissionGroupWithGroupRestDTOMapper.convertCollection(
                    permissionService.findPermissionsForUser(userService.checkId(userId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS)).collectPermissionGroups(),
                    mapperContext);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
