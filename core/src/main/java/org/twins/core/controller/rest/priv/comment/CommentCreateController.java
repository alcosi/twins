package org.twins.core.controller.rest.priv.comment;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.comment.CommentCreateRqDTOv1;
import org.twins.core.dto.rest.comment.CommentCreateRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.comment.CommentCreateRestDTOReversedMapper;
import org.twins.core.mappers.rest.comment.CommentCreateRsRestDTOMapper;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.permission.Permissions;

import java.util.*;

@Tag(description = "", name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.COMMENT_MANAGE, Permissions.COMMENT_CREATE})
public class CommentCreateController extends ApiController {
    private final CommentService commentService;
    private final CommentCreateRsRestDTOMapper commentCreateRsRestDTOMapper;
    private final CommentCreateRestDTOReversedMapper commentRestDTOReverseMapper;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentAddV1", summary = "Add comment and it's attachments by twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin comment", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/comment/twin/{twinId}/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCommentAddV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody CommentCreateRqDTOv1 request) {
        return processComments(twinId, request, Collections.emptyMap());
    }

    @Operation(operationId = "twinCommentAddV1", summary = "Add comment and it's attachments by twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin comment", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/comment/twin/{twinId}/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCommentAddV1Multipart(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = CommentCreateRqDTOv1.class) @RequestPart("request") byte[] requestBytes
    ) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return processComments(twinId, mapRequest(requestBytes, CommentCreateRqDTOv1.class), filesMap);
    }


    protected ResponseEntity<? extends Response> processComments(UUID twinId, CommentCreateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        CommentCreateRsDTOv1 rs = new CommentCreateRsDTOv1();

        try {
            attachmentCreateRestDTOReverseMapper.preProcessAttachments(request.comment.attachments, filesMap);
            TwinCommentEntity comment = commentRestDTOReverseMapper.convert(request.getComment()).setTwinId(twinId);
            rs = commentCreateRsRestDTOMapper.convert(commentService
                    .createComment(comment, attachmentCreateRestDTOReverseMapper.
                            convertCollection(request.getComment().getAttachments())));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
