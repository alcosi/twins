package org.twins.core.controller.rest.priv.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentCreateRqDTOv1;
import org.twins.core.dto.rest.comment.CommentListRsDTOv1;
import org.twins.core.mappers.rest.comment.CommentCreateRestDTOReversedMapper;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.comment.CommentService;

import java.util.List;

@Tag(description = "", name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentCreateController extends ApiController {
    private final CommentService commentService;
    private final CommentRestDTOMapper commentRestDTOMapper;
    private final CommentCreateRestDTOReversedMapper commentRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;


    @ParametersApiUserHeaders
    @Operation(operationId = "commentCreateV1", summary = "Add comments and their attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "list comments", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/comment/v1")
    public ResponseEntity<?> commentCreateV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentListRsDTOv1.class) MapperContext mapperContext,
            @RequestBody CommentCreateRqDTOv1 request) {
        CommentListRsDTOv1 rs = new CommentListRsDTOv1();
        try {
            List<TwinCommentEntity> commentList = commentRestDTOReverseMapper.convertCollection(request.getComments());
            rs
                    .setComments(commentRestDTOMapper.convertCollection(commentService.createComment(commentList)))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
