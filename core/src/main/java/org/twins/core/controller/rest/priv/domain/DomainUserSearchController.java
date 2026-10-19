package org.twins.core.controller.rest.priv.domain;

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
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserSearchRqDTOv1;
import org.twins.core.dto.rest.domain.DomainUserSearchRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainUserRestDTOMapper;
import org.twins.core.mappers.rest.domain.DomainUserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainUserSearchService;
import org.twins.core.service.permission.Permissions;

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
                    .setPagination(paginationMapper.convert(domainUserList))
                    .setUsers(domainUserRestDTOMapper.convertCollection(domainUserList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
