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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentCreateRqDTOv1;
import org.twins.core.dto.rest.comment.CommentCreateRsDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.comment.CommentCreateRestDTOReversedMapper;
import org.twins.core.mappers.rest.comment.CommentCreateRsRestDTOMapper;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.COMMENT_CREATE)
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
    @PostMapping(value = "/private/comment/twin/{twinId}/v1")
    public ResponseEntity<?> twinCommentAddV1(
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody CommentCreateRqDTOv1 request) {
        CommentCreateRsDTOv1 rs = new CommentCreateRsDTOv1();
        try {
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
