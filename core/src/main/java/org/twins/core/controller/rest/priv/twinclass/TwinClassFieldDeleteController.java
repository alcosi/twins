package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinclass.TwinClassFieldsDeleteRqDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_FIELD_MANAGE, Permissions.TWIN_CLASS_FIELD_DELETE})
public class TwinClassFieldDeleteController extends ApiController {

    private final TwinClassFieldService twinClassFieldService;
    private final TwinService twinService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldsDeleteV1", summary = "Delete given twin class fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fields were deleted", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/twin_class_field/v1")
    public ResponseEntity<?> twinClassFieldDeleteV1(
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            @RequestBody TwinClassFieldsDeleteRqDTOv1 rq) {
        Response rs = new Response();

        try {
            if (!force) {
                List<TwinClassFieldEntity> fieldsToDelete = new ArrayList<>();
                KitGrouped<TwinClassFieldEntity, UUID, UUID> fieldsKitGrouped = new KitGrouped<>(twinClassFieldService.findEntitiesSafe(rq.getFieldIds()), TwinClassFieldEntity::getId, TwinClassFieldEntity::getTwinClassId);

                for (var twinClassId : fieldsKitGrouped.getGroupedKeySet()) {
                    if (twinService.existsByTwinClassId(twinClassId)) {
                        log.info("Twin with twinClassId {} already exists, can't delete its fields", twinClassId);
                        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_IN_USE, ErrorCodeTwins.TWIN_CLASS_FIELD_IN_USE.getMessage());
                    }

                    fieldsToDelete.addAll(fieldsKitGrouped.getGrouped(twinClassId));
                }

                twinClassFieldService.deleteSafe(fieldsToDelete.stream().map(TwinClassFieldEntity::getId).toList());
            } else {
                twinClassFieldService.deleteSafe(rq.getFieldIds());
            }
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
