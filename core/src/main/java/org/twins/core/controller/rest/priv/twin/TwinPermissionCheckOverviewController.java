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
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.permission.PermissionCheckForTwinOverviewResult;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionCheckOverviewDTOMapper;
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
            @MapperContextBinding(roots = PermissionCheckOverviewDTOMapper.class, response = PermissionCheckOverviewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody PermissionCheckOverviewRqDTOv1 request) {
        PermissionCheckOverviewRsDTOv1 rs = new PermissionCheckOverviewRsDTOv1();
        try {
            PermissionCheckForTwinOverviewResult permissionCheckOverviewResult = permissionService.checkTwinAndUserForPermissions(request.userId(), twinId, request.permissionId());
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
