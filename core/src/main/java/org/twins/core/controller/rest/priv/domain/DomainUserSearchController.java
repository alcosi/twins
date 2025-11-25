package org.twins.core.controller.rest.priv.domain;

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
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.domain.DomainUserSearchRqDTOv1;
import org.twins.core.dto.rest.domain.DomainUserSearchRsDTOv1;
import org.twins.core.dto.rest.domain.DomainUserViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainUserRestDTOMapper;
import org.twins.core.mappers.rest.domain.DomainUserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainUserSearchService;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUserSearchController extends ApiController {
    private final DomainUserSearchService domainUserSearchService;
    private final DomainUserSearchDTOReverseMapper domainUserSearchDTOReverseMapper;
    private final DomainUserRestDTOMapper domainUserRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final DomainUserService domainUserService;
    private final AuthService authService;
    private final PermissionService permissionService;

    @ProtectedBy({Permissions.DOMAIN_USER_MANAGE, Permissions.DOMAIN_USER_VIEW})
    @ParametersApiUserHeaders
    @Operation(operationId = "domainUserSearchListV1", summary = "Return a list of users by current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainUserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/user/search/v1")
    public ResponseEntity<?> domainUserSearchListV1(
            @MapperContextBinding(roots = DomainUserRestDTOMapper.class, response = DomainUserSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DomainUserSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        DomainUserSearchRsDTOv1 rs = new DomainUserSearchRsDTOv1();
        try {
            PaginationResult<DomainUserEntity> domainUserList = domainUserSearchService
                    .findDomainUser(domainUserSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setUsers(domainUserRestDTOMapper.convertCollection(domainUserList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(domainUserList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


    @ParametersApiUserHeaders
    @Operation(operationId = "domainUserViewV1", summary = "Return the user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainUserViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/domain/user/{userId}/v1")
    public ResponseEntity<?> domainUserViewV1(
            @MapperContextBinding(roots = DomainUserRestDTOMapper.class, response = DomainUserViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.USER_ID) @PathVariable("userId") UUID userId) {
        DomainUserViewRsDTOv1 rs = new DomainUserViewRsDTOv1();
        try {
            permissionService.checkUserIsCurrentOrHasPermission(userId, true, Permissions.DOMAIN_USER_MANAGE, Permissions.DOMAIN_USER_VIEW);
            DomainUserEntity domainUser = domainUserService.findByUserId(userId);
            rs
                    .setUser(domainUserRestDTOMapper.convert(domainUser, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "domainCurrentUserViewV1", summary = "Returns current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainUserViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/domain/user/v1")
    public ResponseEntity<?> domainCurrentUserViewV1(
            @MapperContextBinding(roots = DomainUserRestDTOMapper.class, response = DomainUserViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext) {
        DomainUserViewRsDTOv1 rs = new DomainUserViewRsDTOv1();
        try {
            DomainUserEntity domainUser = domainUserService.getCurrentUser();
            rs
                    .setUser(domainUserRestDTOMapper.convert(domainUser, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
