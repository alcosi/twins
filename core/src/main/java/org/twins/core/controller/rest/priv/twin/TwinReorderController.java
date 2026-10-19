package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.twin.TwinReorderRqDTOv1;
import org.twins.core.dto.rest.twin.TwinReorderRsDTOv1;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_UPDATE, Permissions.TWIN_LINK_MANAGE})
public class TwinReorderController extends ApiController {

    @ParametersApiUserHeaders
    @Operation(operationId = "twinReorderV1", summary = "Batch reorder of twins by user defined order. "
            + "Moves without linkId reorder head children of the moved twin's head; moves with linkId reorder "
            + "src twins linked by that link type. STUB: contract only, returns 501 until service is implemented")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reorder result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinReorderRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/reorder/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinReorderV1(
            @RequestBody @Valid TwinReorderRqDTOv1 request) {
        TwinReorderRsDTOv1 rs = new TwinReorderRsDTOv1();
        try {
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "Twin reorder is not implemented yet");
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
    }
}
