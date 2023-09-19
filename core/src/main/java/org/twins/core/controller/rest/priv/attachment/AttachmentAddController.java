package org.twins.core.controller.rest.priv.attachment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentAddRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentAddRsDTOv1;
import org.twins.core.dto.rest.twin.TwinCreateRsDTOv1;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = "attachment")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AttachmentAddController extends ApiController {
    private final AuthService authService;
    private final AttachmentService attachmentService;
    private final TwinService twinService;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentAddV1", summary = "Add attachment to twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{twinId}/attachment/v1", method = RequestMethod.POST)
    public ResponseEntity<?> attachmentAddV1(
            @Parameter(name = "twinId", in = ParameterIn.PATH, required = true, example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody AttachmentAddRqDTOv1 request) {
        AttachmentAddRsDTOv1 rs = new AttachmentAddRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            TwinAttachmentEntity attachmentEntity = attachmentService.addAttachment(
                    new TwinAttachmentEntity()
                            .setTwinId(twinService.checkTwinId(twinId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS))
                            .setCreatedByUserId(apiUser.getUser().getId())
                            .setStorageLink(request.storageLink));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
