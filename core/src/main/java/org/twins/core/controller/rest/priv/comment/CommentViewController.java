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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentViewRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.comment.CommentViewRestDTOMapper;
import org.twins.core.service.comment.CommentService;

import java.util.UUID;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentViewController extends ApiController {
    final CommentService commentService;
    final CommentViewRestDTOMapper commentViewRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentV1", summary = "Returns comment by comment id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/comment/{commentId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinCommentV1(
            @RequestParam(name = RestRequestParam.showCommentMode, defaultValue = CommentViewRestDTOMapper.Mode._DETAILED) CommentViewRestDTOMapper.Mode showCommentMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._SHORT) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @Parameter(example = DTOExamples.TWIN_COMMENT) @PathVariable UUID commentId) {
        CommentViewRsDTOv1 rs = new CommentViewRsDTOv1();
        try {
            TwinCommentEntity comment = commentService.findEntitySafe(commentId);
            MapperContext mapperContext = new MapperContext()
                    .setMode(showCommentMode)
                    .setMode(showAttachmentMode);
            rs
                    .setComment(commentViewRestDTOMapper.convert(comment, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
