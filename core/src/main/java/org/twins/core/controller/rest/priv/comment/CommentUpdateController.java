package org.twins.core.controller.rest.priv.comment;

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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.domain.comment.CommentUpdate;
import org.twins.core.dto.rest.comment.CommentListRsDTOv1;
import org.twins.core.dto.rest.comment.CommentUpdateDTOv1;
import org.twins.core.dto.rest.comment.CommentUpdateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.service.comment.CommentService;

import java.util.ArrayList;
import java.util.List;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentUpdateController extends ApiController {
    private final CommentService commentService;
    private final CommentRestDTOMapper commentRestDTOMapper;
    private final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "commentUpdateV1", summary = "Update comment and it's attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/comment/v1")
    public ResponseEntity<?> commentUpdateV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentListRsDTOv1.class) MapperContext mapperContext,
            @RequestBody CommentUpdateRqDTOv1 request) {
        CommentListRsDTOv1 rs = new CommentListRsDTOv1();
        try {
            List<CommentUpdate> comments = new ArrayList<>();
            for (CommentUpdateDTOv1 comment : request.getComments()) {
                comments.add(new CommentUpdate()
                        .setId(comment.getId())
                        .setTwinId(comment.getTwinId())
                        .setComment(comment.getText())
                        .setCudAttachments(attachmentCUDRestDTOReverseMapperV2.convert(comment.getAttachments())));
            }
            List<TwinCommentEntity> twinCommentEntities = commentService.updateComment(comments);
            rs
                    .setComments(commentRestDTOMapper.convertCollection(twinCommentEntities, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
