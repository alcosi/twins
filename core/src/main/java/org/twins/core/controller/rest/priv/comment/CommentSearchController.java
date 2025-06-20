package org.twins.core.controller.rest.priv.comment;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentSearchRqDTOv1;
import org.twins.core.dto.rest.comment.CommentSearchRsDTOv1;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.comment.CommentSearchRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.service.comment.CommentService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.COMMENT_MANAGE, Permissions.COMMENT_VIEW})
public class CommentSearchController extends ApiController {
    private final CommentService commentService;
    private final CommentSearchRestDTOMapper searchRestDTOMapper;
    private final CommentRestDTOMapper viewRestDTOMapper;

    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "commentSearchV1", summary = "Returns comment search result in current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment status list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/comment/search/v1")
    public ResponseEntity<?> commentSearchV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody CommentSearchRqDTOv1 request) {
        CommentSearchRsDTOv1 rs = new CommentSearchRsDTOv1();
        try {
            PaginationResult<TwinCommentEntity> twinStatusList = commentService
                    .findComments(searchRestDTOMapper.convert(request), pagination);
            rs
                    .setComments(viewRestDTOMapper.convertCollection(twinStatusList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinStatusList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
