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
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
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
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentListRsDTOv1;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.COMMENT_MANAGE, Permissions.COMMENT_VIEW})
public class CommentListController extends ApiController {
    private final CommentService commentService;
    private final CommentRestDTOMapper commentRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentListV1", summary = "Returns comment list by twin id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/comment/twin/{twinId}/v1")
    public ResponseEntity<?> twinCommentListV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentListRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams(sortAsc = false, sortField = TwinCommentEntity.Fields.createdAt) SimplePagination pagination,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId) {
        CommentListRsDTOv1 rs = new CommentListRsDTOv1();
        try {
            PaginationResult<TwinCommentEntity> commentList = commentService.findComment(twinId, pagination);
            rs
                    .setComments(commentRestDTOMapper.convertCollection(commentList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(commentList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
