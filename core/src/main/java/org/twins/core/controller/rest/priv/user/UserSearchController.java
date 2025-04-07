package org.twins.core.controller.rest.priv.user;

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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserSearchRqDTOv1;
import org.twins.core.dto.rest.user.UserSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.user.UserSearchDTOReverseMapper;
import org.twins.core.service.user.UserSearchService;

@Tag(description = "", name = ApiTag.USER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserSearchController extends ApiController {
    private final UserSearchService userSearchService;
    private final UserRestDTOMapper userRestDTOMapper;
    private final UserSearchDTOReverseMapper userSearchDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "userSearchV1", summary = "Return a list of users by search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = UserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/user/search/v1")
    public ResponseEntity<?> UserSearchV1(
            @MapperContextBinding(roots = UserRestDTOMapper.class, response = UserSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
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
}
