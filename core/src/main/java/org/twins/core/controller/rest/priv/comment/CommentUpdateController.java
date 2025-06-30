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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentRsDTOv1;
import org.twins.core.dto.rest.comment.CommentUpdateRqDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

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
    @PutMapping(value = "/private/comment/{commentId}/v1")
    public ResponseEntity<?> twinCommentUpdateV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_COMMENT_ID) @PathVariable UUID commentId,
            @RequestBody CommentUpdateRqDTOv1 request) {
        CommentRsDTOv1 rs = new CommentRsDTOv1();
        try {
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
