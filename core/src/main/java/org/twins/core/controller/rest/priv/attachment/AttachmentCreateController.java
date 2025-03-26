package org.twins.core.controller.rest.priv.attachment;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentCreateRqDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentListRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestDTOMapper;
import org.twins.core.service.attachment.AttachmentService;

import java.util.*;
import java.util.stream.Collectors;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AttachmentCreateController extends ApiController {
    private final AttachmentService attachmentService;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;
    private final AttachmentRestDTOMapper attachmentRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "attachmentCreateV1", summary = "Add attachments to twins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/attachment/v1")
    public ResponseEntity<?> attachmentCreateV1(
            @RequestBody AttachmentCreateRqDTOv1 request) {
        AttachmentListRsDTOv1 rs = new AttachmentListRsDTOv1();
        try {
            List<TwinAttachmentEntity> attachments = attachmentService.addAttachments(attachmentCreateRestDTOReverseMapper.convertCollection(request.getAttachments()).stream()
                    .collect(Collectors.groupingBy(
                            TwinAttachmentEntity::getTwinId
                    )));
            rs
                    .setAttachments(attachmentRestDTOMapper.convertCollection(attachments));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
