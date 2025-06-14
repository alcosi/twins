package org.twins.core.controller.rest.priv.attachment;

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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentAddRsDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCreateRqDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ATTACHMENT_MANAGE, Permissions.ATTACHMENT_CREATE})
public class AttachmentAddController extends ApiController {
    private final AuthService authService;
    private final AttachmentService attachmentService;
    private final TwinService twinService;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentCreateV1", summary = "Add attachment to twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentAddRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/{twinId}/attachment/v1")
    public ResponseEntity<?> attachmentCreateV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody AttachmentCreateRqDTOv1 request) {
        AttachmentAddRsDTOv1 rs = new AttachmentAddRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.setAttachmentIdList(attachmentService.addAttachments(
                            attachmentCreateRestDTOReverseMapper.convertCollection(request.getAttachments()), twinService.findEntitySafe(twinId))
                    .stream().map(TwinAttachmentEntity::getId).toList());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
