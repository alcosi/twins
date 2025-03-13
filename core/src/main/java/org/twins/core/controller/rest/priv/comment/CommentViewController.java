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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.service.comment.CommentService;

import java.util.UUID;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentViewController extends ApiController {
    private final CommentService commentService;
    private final CommentRestDTOMapper commentRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentV1", summary = "Returns comment by comment id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/comment/{commentId}/v1")
    public ResponseEntity<?> twinCommentV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_COMMENT_ID) @PathVariable UUID commentId) {
        CommentRsDTOv1 rs = new CommentRsDTOv1();
        try {
            TwinCommentEntity comment = commentService.findEntitySafe(commentId);
            rs
                    .setComment(commentRestDTOMapper.convert(comment, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
