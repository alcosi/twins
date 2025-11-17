package org.twins.core.controller.rest.priv.user;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserSearchConfiguredDTOv1;
import org.twins.core.dto.rest.user.UserSearchConfiguredRqDTOv1;
import org.twins.core.dto.rest.user.UserSearchRqDTOv1;
import org.twins.core.dto.rest.user.UserSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.user.UserSearchConfiguredDTOReverseMapper;
import org.twins.core.mappers.rest.user.UserSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.user.UserSearchService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.USER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.USER_MANAGE, Permissions.USER_VIEW})
public class UserSearchController extends ApiController {
    private final UserSearchService userSearchService;
    private final UserRestDTOMapper userRestDTOMapper;
    private final UserSearchDTOReverseMapper userSearchDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final UserSearchConfiguredDTOReverseMapper userSearchConfiguredDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userSearchV1", summary = "Return a list of users by search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/user/search/v1")
    public ResponseEntity<?> userSearchV1(
            @MapperContextBinding(roots = UserRestDTOMapper.class, response = UserSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortField = UserEntity.Fields.createdAt) SimplePagination pagination,
            @RequestBody UserSearchRqDTOv1 request) {
        UserSearchRsDTOv1 rs = new UserSearchRsDTOv1();
        try {
            PaginationResult<UserEntity> users = userSearchService.findUsers(userSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setUsers(userRestDTOMapper.convertCollection(users.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(users))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "userSearchConfiguredV1", summary = "Return a list of users by configured search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/user/search/{searchId}/v1")
    public ResponseEntity<?> userSearchConfiguredV1(
            @MapperContextBinding(roots = UserRestDTOMapper.class, response = UserSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams(sortField = UserEntity.Fields.createdAt) SimplePagination pagination,
            @Parameter(example = DTOExamples.SEARCH_ID) @PathVariable UUID searchId,
            @RequestBody UserSearchConfiguredRqDTOv1 request) {
        UserSearchRsDTOv1 rs = new UserSearchRsDTOv1();
        try {
            UserSearchConfiguredDTOv1 search = request.getSearch();
            PaginationResult<UserEntity> users = userSearchService.findUsers(
                    searchId,
                    userSearchConfiguredDTOReverseMapper.convert(search),
                    search != null ? search.getParams() : null,
                    pagination
            );
            rs
                    .setUsers(userRestDTOMapper.convertCollection(users.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(users))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
