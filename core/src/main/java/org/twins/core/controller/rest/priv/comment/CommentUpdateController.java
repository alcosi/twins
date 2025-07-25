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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.comment.CommentRsDTOv1;
import org.twins.core.dto.rest.comment.CommentUpdateRqDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.permission.Permissions;

import java.util.*;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.COMMENT_MANAGE, Permissions.COMMENT_UPDATE})
public class CommentUpdateController extends ApiController {
    private final CommentService commentService;
    private final CommentRestDTOMapper commentRestDTOMapper;
    private final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentUpdateV1", summary = "Update comment and it's attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/comment/{commentId}/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCommentUpdateV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_COMMENT_ID) @PathVariable UUID commentId,
            @RequestBody CommentUpdateRqDTOv1 request) {
        return processUpdate(mapperContext, commentId, request, Collections.emptyMap());
    }

    @Operation(operationId = "twinCommentUpdateV1", summary = "Update comment and it's attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/comment/{commentId}/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCommentUpdateV1Multipart(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_COMMENT_ID) @PathVariable UUID commentId,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = CommentUpdateRqDTOv1.class) @RequestPart("request") byte[] requestBytes
    ) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return processUpdate(mapperContext, commentId, mapRequest(requestBytes, CommentUpdateRqDTOv1.class), filesMap);
    }

    protected ResponseEntity<? extends Response> processUpdate(MapperContext mapperContext, UUID commentId, CommentUpdateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        CommentRsDTOv1 rs = new CommentRsDTOv1();
        try {
            if (request.getComment() != null) {
                attachmentCUDRestDTOReverseMapperV2.preProcessAttachments(request.comment.getAttachments(), Collections.emptyMap());
            }
            EntityCUD<TwinAttachmentEntity> attachmentCUD = attachmentCUDRestDTOReverseMapperV2.convert(request.getComment().getAttachments());
            TwinCommentEntity twinComment = commentService.updateComment(commentId, request.getComment().getText(), attachmentCUD);
            rs
                    .setComment(commentRestDTOMapper.convert(twinComment, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
