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
import org.twins.core.domain.permission.PermissionCheckOverviewResult;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRsDTOv1;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionCheckOverviewDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionCheckOverviewDTOReverseMapper;
import org.twins.core.mappers.rest.twin.*;
import org.twins.core.service.permission.PermissionService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionCheckOverviewController extends ApiController {

    final PermissionService permissionService;
    final PermissionCheckOverviewDTOReverseMapper permissionCheckOverviewDTOReverseMapper;
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
            @RequestParam(name = RestRequestParam.showPermissionSchemaMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._SHORT) TwinBaseRestDTOMapper.TwinMode showPermissionSchemaMode,
            @RequestParam(name = RestRequestParam.showPermissionMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._SHORT) TwinBaseRestDTOMapper.TwinMode showPermissionMode,
            @RequestParam(name = RestRequestParam.showPermissionGroupMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._SHORT) TwinBaseRestDTOMapper.TwinMode showPermissionGroupMode,
            @RequestParam(name = RestRequestParam.showUserGroupMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._SHORT) TwinBaseRestDTOMapper.TwinMode showUserGroupMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleUserMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._SHORT) TwinBaseRestDTOMapper.TwinMode showSpaceRoleUserMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleUserGroupMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._SHORT) TwinBaseRestDTOMapper.TwinMode showSpaceRoleUserGroupMode,
            @RequestParam(name = RestRequestParam.showSpaceRoleMode, defaultValue = TwinBaseRestDTOMapper.TwinMode._SHORT) TwinBaseRestDTOMapper.TwinMode showSpaceRoleMode,
            @RequestBody PermissionCheckOverviewRqDTOv1 request) {
        PermissionCheckOverviewRsDTOv1 rs = new PermissionCheckOverviewRsDTOv1();
        try {
            PermissionCheckOverviewResult permissionCheckOverviewResult = permissionService.checkTwinAndUserForPermissions(permissionCheckOverviewDTOReverseMapper.convert(request).setTwinId(twinId));
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
