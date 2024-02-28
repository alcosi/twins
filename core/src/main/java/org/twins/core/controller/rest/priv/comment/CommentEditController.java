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
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.comment.CommentUpdateRqDTOv1;
import org.twins.core.mappers.rest.comment.CommentCudRestDTOReversedMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.comment.CommentService;

import java.util.UUID;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentEditController extends ApiController {
    final AuthService authService;
    final CommentService commentService;
    final CommentCudRestDTOReversedMapper commentCudRestDTOReversedMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentUpdateV1", summary = "Update comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentUpdateRqDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin/{commentId}/comment/v1", method = RequestMethod.PUT)
    public ResponseEntity<?> twinCommentUpdateV1(
            @Parameter(example = DTOExamples.TWIN_COMMENT) @PathVariable UUID commentId,
            @RequestBody CommentUpdateRqDTOv1 request) {
        Response rs = new Response();
        try {
            EntityCUD<TwinAttachmentEntity> attachmentUpdate = new EntityCUD<>();
            commentCudRestDTOReversedMapper.map(request, attachmentUpdate, null);
            commentService.updateComment(commentId, request.getText(), attachmentUpdate);
        } catch (ServiceException se) {
            se.printStackTrace();
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
