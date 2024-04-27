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
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusCreateRestDTOReverseMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinflowTransitionCreateController extends ApiController {
    final AuthService authService;
    final UserService userService;
    final TwinStatusCreateRestDTOReverseMapper twinStatusCreateRestDTOReverseMapper;
    final TwinStatusService twinStatusService;
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionCreateV1", summary = "Create new transition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twinflow/{twinflowId}/transition/v1", method = RequestMethod.POST)
    public ResponseEntity<?> transitionCreateV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestParam(name = RestRequestParam.showStatusMode, defaultValue = TwinStatusRestDTOMapper.Mode._HIDE) TwinStatusRestDTOMapper.Mode showStatusMode,
            @RequestBody TwinStatusCreateRqDTOv1 request) {
        TwinStatusCreateRsDTOv1 rs = new TwinStatusCreateRsDTOv1();
        try {
            TwinStatusEntity twinStatusEntity = twinStatusCreateRestDTOReverseMapper.convert(request.setTwinClassId(twinClassId));
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(true)
                    .setMode(showStatusMode);
            twinStatusEntity = twinStatusService.createStatus(twinStatusEntity, request.getName(), request.getDescription());
            rs
                    .setTwinStatus(twinStatusRestDTOMapper.convert(twinStatusEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
