package org.twins.core.controller.rest.priv.twin;

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
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.permission.PermissionCheckForTwinOverviewResult;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionCheckOverviewDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleByUserDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserGroupDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.service.permission.PermissionService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinPermissionCheckOverviewController extends ApiController {

    final PermissionService permissionService;
    final PermissionCheckOverviewDTOMapper permissionCheckOverviewDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "permissonCheckOverviewV1", summary = "Permisson check overview by twinId & userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/{twinId}/permisson_check_overview/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> permissonCheckOverview(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestParam(name = RestRequestParam.showPermissionSchemaMode, defaultValue = PermissionSchemaRestDTOMapper.Mode._DETAILED) PermissionSchemaRestDTOMapper.Mode showPermissionSchemaMode,
            @RequestParam(name = RestRequestParam.showPermissionMode, defaultValue = PermissionRestDTOMapper.Mode._DETAILED) PermissionRestDTOMapper.Mode showPermissionMode,
            @RequestParam(name = RestRequestParam.showPermissionGroupMode, defaultValue = PermissionGroupRestDTOMapper.Mode._DETAILED) PermissionGroupRestDTOMapper.Mode showPermissionGroupMode,
            @RequestParam(name = RestRequestParam.showUserGroupMode, defaultValue = UserGroupRestDTOMapper.Mode._DETAILED) UserGroupRestDTOMapper.Mode showUserGroupMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleUserMode, defaultValue = SpaceRoleUserDTOMapper.Mode._DETAILED) SpaceRoleUserDTOMapper.Mode showSpaceRoleUserMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleUserGroupMode, defaultValue = SpaceRoleUserGroupDTOMapper.Mode._DETAILED) SpaceRoleUserGroupDTOMapper.Mode showSpaceRoleUserGroupMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleMode, defaultValue = SpaceRoleByUserDTOMapper.Mode._DETAILED) SpaceRoleByUserDTOMapper.Mode showSpaceRoleMode,
            @RequestBody PermissionCheckOverviewRqDTOv1 request) {
        PermissionCheckOverviewRsDTOv1 rs = new PermissionCheckOverviewRsDTOv1();
        try {
            PermissionCheckForTwinOverviewResult permissionCheckOverviewResult = permissionService.checkTwinAndUserForPermissions(request.userId(), twinId, request.permissionId());
            MapperContext mapperContext = new MapperContext()
                    .setMode(showPermissionSchemaMode)
                    .setMode(showPermissionMode)
                    .setMode(showPermissionGroupMode)
                    .setMode(showUserGroupMode)
                    .setMode(showSpaceRoleUserMode)
                    .setMode(showSpaceRoleUserGroupMode)
                    .setMode(showSpaceRoleMode);
            rs = permissionCheckOverviewDTOMapper.convert(permissionCheckOverviewResult, mapperContext);
        } catch (ServiceException se) {
            se.printStackTrace();
            return createErrorRs(se, rs);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
