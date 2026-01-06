package org.twins.core.controller.rest.priv.attachment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.attachment.AttachmentAddRsDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentCreateRqDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.util.*;

@Tag(description = "", name = ApiTag.ATTACHMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.ATTACHMENT_MANAGE, Permissions.ATTACHMENT_CREATE})
public class AttachmentAddController extends ApiController {
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
    @PostMapping(value = "/private/twin/{twinId}/attachment/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> attachmentCreateV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody AttachmentCreateRqDTOv1 request) {
        return createAttachment(twinId, request, Collections.emptyMap());
    }


    @SneakyThrows
    @Operation(operationId = "attachmentCreateV1", summary = "Add attachment to twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = AttachmentAddRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/{twinId}/attachment/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> attachmentCreateV1Multipart(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = AttachmentCreateRqDTOv1.class) @RequestPart("request") byte[] requestBytes
    ) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return createAttachment(twinId, mapRequest(requestBytes, AttachmentCreateRqDTOv1.class), filesMap);
    }


    protected ResponseEntity<? extends Response> createAttachment(UUID twinId, AttachmentCreateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        AttachmentAddRsDTOv1 rs = new AttachmentAddRsDTOv1();
        try {
            attachmentCreateRestDTOReverseMapper.preProcessAttachments(request.attachments, filesMap);
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
