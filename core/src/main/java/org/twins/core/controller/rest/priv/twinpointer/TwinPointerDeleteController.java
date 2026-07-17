package org.twins.core.controller.rest.priv.twinpointer;

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
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinpointer.TwinPointerDeleteRqDTOv1;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinPointerService;

@Tag(name = ApiTag.TWIN_POINTER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TWIN_POINTER_DELETE)
public class TwinPointerDeleteController extends ApiController {
    private final TwinPointerService twinPointerService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinPointerDeleteV1", summary = "Twin pointer delete (batch by id list)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin pointer deleted", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_pointer/delete/v1")
    public ResponseEntity<?> twinPointerDeleteV1(
            @RequestBody TwinPointerDeleteRqDTOv1 request) {
        Response rs = new Response();
        try {
            twinPointerService.deleteSafe(request.getTwinPointerIdList());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
