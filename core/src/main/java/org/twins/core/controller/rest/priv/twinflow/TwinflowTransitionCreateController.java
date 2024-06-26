package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionCreateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionCreateRsDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowTransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowTransitionBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowTransitionCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinflow.TwinflowTransitionService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinflowTransitionCreateController extends ApiController {
    final AuthService authService;
    final UserService userService;
    final TwinflowTransitionCreateRestDTOReverseMapper twinflowTransitionCreateRestDTOReverseMapper;
    final TwinflowTransitionService twinflowTransitionService;
    final TwinflowTransitionBaseV2RestDTOMapper twinflowTransitionBaseV2RestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionCreateV1", summary = "Create new transition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twinflow/{twinflowId}/transition/v1", method = RequestMethod.POST)
    public ResponseEntity<?> transitionCreateV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinflowId,
            @RequestParam(name = RestRequestParam.showTwinflowTransitionMode, defaultValue = TwinflowTransitionBaseV1RestDTOMapper.TwinflowTransitionMode._SHORT) TwinflowTransitionBaseV1RestDTOMapper.TwinflowTransitionMode showTwinflowTransitionMode,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._SHORT) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showPermissionMode, defaultValue = PermissionRestDTOMapper.Mode._HIDE) PermissionRestDTOMapper.Mode showPermissionMode,
            @RequestBody TwinflowTransitionCreateRqDTOv1 request) {
        TwinflowTransitionCreateRsDTOv1 rs = new TwinflowTransitionCreateRsDTOv1();
        try {
            TwinflowTransitionEntity twinflowTransitionEntity = twinflowTransitionCreateRestDTOReverseMapper.convert(request.setTwinflowId(twinflowId));
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(true)
                    .setMode(showTwinflowTransitionMode)
                    .setMode(showStatusMode)
                    .setMode(showUserMode)
                    .setMode(showPermissionMode);
            twinflowTransitionEntity = twinflowTransitionService.createTwinflowTransition(twinflowTransitionEntity, request.getName());
            rs
                    .setTransition(twinflowTransitionBaseV2RestDTOMapper.convert(twinflowTransitionEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
