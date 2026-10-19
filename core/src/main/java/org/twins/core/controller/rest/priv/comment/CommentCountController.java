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
import org.twins.core.dto.rest.comment.CommentCountRqDTOv1;
import org.twins.core.dto.rest.comment.CommentCountRsDTOv1;
import org.twins.core.mappers.rest.comment.CommentCountRestDTOMapper;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.comment.CommentSearchDTORestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.comment.CommentSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.COMMENT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.COMMENT_MANAGE, Permissions.COMMENT_VIEW})
public class CommentCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final CommentSearchDTORestDTOMapper commentSearchDTORestDTOMapper;
    private final CommentCountRestDTOMapper commentCountRestDTOMapper;
    private final CommentSearchService commentSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "commentCountV1", summary = "Returns comment count grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CommentCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/comment/count/v1")
    public ResponseEntity<?> commentCountV1(
            @MapperContextBinding(roots = CommentRestDTOMapper.class, response = CommentCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid CommentCountRqDTOv1 request) {
        CommentCountRsDTOv1 rs = new CommentCountRsDTOv1();
        try {
            var results = commentSearchService
                    .countByGroupFields(commentSearchDTORestDTOMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(commentCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
