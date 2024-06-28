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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentUpdateRqDTOv1;
import org.twins.core.dto.rest.comment.CommentViewRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.comment.CommentViewRestDTOMapper;
import org.twins.core.service.comment.CommentService;

import java.util.UUID;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentEditController extends ApiController {
    final CommentService commentService;
    final CommentViewRestDTOMapper commentViewRestDTOMapper;
    final AttachmentCUDRestDTOReverseMapperV2 attachmentCUDRestDTOReverseMapperV2;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentUpdateV1", summary = "Update comment and it's attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/comment/{commentId}/v1")
    public ResponseEntity<?> twinCommentUpdateV1(
            @Parameter(example = DTOExamples.TWIN_COMMENT) @PathVariable UUID commentId,
            @RequestBody CommentUpdateRqDTOv1 request) {
        CommentViewRsDTOv1 rs = new CommentViewRsDTOv1();
        try {
            EntityCUD<TwinAttachmentEntity> attachmentCUD = attachmentCUDRestDTOReverseMapperV2.convert(request.getAttachments());
            TwinCommentEntity twinComment = commentService.updateComment(commentId, request.getText(), attachmentCUD);
            rs
                    .setComment(commentViewRestDTOMapper.
                            convert(twinComment,
                                    new MapperContext()
                                            .setMode(CommentViewRestDTOMapper.Mode.DETAILED)
                                            .setMode(MapperMode.AttachmentMode.DETAILED)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
