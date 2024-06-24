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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.comment.CommentListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.attachment.AttachmentViewRestDTOMapper;
import org.twins.core.mappers.rest.comment.CommentViewRestDTOMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.pagination.PaginationResult;

import java.util.UUID;

import static org.cambium.common.util.PaginationUtils.*;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CommentListController extends ApiController {
    final CommentService commentService;
    final CommentViewRestDTOMapper commentViewRestDTOMapper;
    final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCommentListV1", summary = "Returns comment list by twin id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/comment/twin/{twinId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinCommentListV1(
            @RequestParam(name = RestRequestParam.lazyRelation, defaultValue = "true") boolean lazyRelation,
            @RequestParam(name = RestRequestParam.sortDirection, defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(name = RestRequestParam.showUserMode, defaultValue = UserRestDTOMapper.Mode._SHORT) UserRestDTOMapper.Mode showUserMode,
            @RequestParam(name = RestRequestParam.showCommentMode, defaultValue = CommentViewRestDTOMapper.Mode._SHORT) CommentViewRestDTOMapper.Mode showCommentMode,
            @RequestParam(name = RestRequestParam.showAttachmentMode, defaultValue = AttachmentViewRestDTOMapper.Mode._SHORT) AttachmentViewRestDTOMapper.Mode showAttachmentMode,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId) {
        CommentListRsDTOv1 rs = new CommentListRsDTOv1();
        try {
            PaginationResult<TwinCommentEntity> commentList = commentService.findComment(twinId, createSimplePagination(offset, limit, Sort.by(direction, TwinCommentEntity.Fields.createdAt)));
            MapperContext mapperContext = new MapperContext()
                    .setLazyRelations(lazyRelation)
                    .setMode(showUserMode)
                    .setMode(showCommentMode)
                    .setMode(showAttachmentMode);
            rs
                    .setComments(commentViewRestDTOMapper.convertList(commentList.getList(), mapperContext))
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
