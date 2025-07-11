package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRsRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinSketchService;

import java.util.List;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_SKETCH_CREATE})
public class TwinSketchCreateController extends ApiController {
    private final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;
    private final TwinSketchService twinSketchService;


    @ParametersApiUserHeaders
    @Operation(operationId = "twinSketchCreateV1", summary = "Create twin sketch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin sketch created", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/sketch/v1")
    public ResponseEntity<?> twinSketchCreateV1(
            @RequestBody TwinBatchCreateRqDTOv1 request) {
        Response rs = new Response();
        try {
            List<TwinCreate> twinCreates = twinCreateRqRestDTOReverseMapper.convertCollection(request.getTwins());
            twinSketchService.createTwinSketch(twinCreates);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
