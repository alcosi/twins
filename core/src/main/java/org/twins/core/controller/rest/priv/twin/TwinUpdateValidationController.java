package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.exception.TwinFieldValidationException;
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.TwinSaveRsV1;
import org.twins.core.dto.rest.twin.TwinUpdateRqDTOv1;
import org.twins.core.mappers.rest.twin.TwinUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_UPDATE})
public class TwinUpdateValidationController extends ApiController {
    private final TwinService twinService;
    private final TwinUpdateRestDTOReverseMapper twinUpdateRestDTOReverseMapper;

    /**
     * Endpoint for validating an updated twin from a JSON request body.
     */
    @ParametersApiUserHeaders
    @Operation(summary = "twinUpdateValidationV1", description = "Validates a twin using a standard JSON payload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TwinSaveRsV1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/twin/{twinId}/validate/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinValidate(@Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
                                          @RequestBody TwinUpdateRqDTOv1 request) {
        return validateTwin(request, twinId);
    }

    protected ResponseEntity<Response> validateTwin(TwinUpdateRqDTOv1 request, UUID twinId) {
        TwinSaveRsV1 rs = new TwinSaveRsV1();
        try {
            TwinEntity dbTwinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            TwinUpdate twinUpdate = twinUpdateRestDTOReverseMapper.convert(Pair.of(request.setTwinId(twinId), dbTwinEntity))
                    .setCheckEditPermission(true);
            twinService.validateFields(dbTwinEntity, twinUpdate.getFields(), false);
        } catch (TwinFieldValidationException ve) {
            return createErrorRs(ve, rs, HttpStatus.OK);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
