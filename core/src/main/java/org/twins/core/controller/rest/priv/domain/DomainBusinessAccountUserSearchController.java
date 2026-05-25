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
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserSearchRqDTOv1;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserSearchRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainBusinessAccountUserRestDTOMapper;
import org.twins.core.mappers.rest.domain.DomainBusinessAccountUserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainBusinessAccountUserSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_USER_MANAGE, Permissions.DOMAIN_USER_VIEW})
public class DomainBusinessAccountUserSearchController extends ApiController {
    private final DomainBusinessAccountUserSearchService domainBusinessAccountUserSearchService;
    private final DomainBusinessAccountUserSearchDTOReverseMapper domainBusinessAccountUserSearchDTOReverseMapper;
    private final DomainBusinessAccountUserRestDTOMapper domainBusinessAccountUserRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainBusinessAccountUserSearchV1", summary = "Return a list of domain business account users by search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainBusinessAccountUserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/business_account_user/search/v1")
    public ResponseEntity<?> domainBusinessAccountUserSearchV1(
            @MapperContextBinding(roots = DomainBusinessAccountUserRestDTOMapper.class, response = DomainBusinessAccountUserSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainBusinessAccountUserSearchRqDTOv1 request) {
        DomainBusinessAccountUserSearchRsDTOv1 rs = new DomainBusinessAccountUserSearchRsDTOv1();
        try {
            PaginationResult<DomainBusinessAccountUserEntity> result = domainBusinessAccountUserSearchService
                    .findDomainBusinessAccountUsers(domainBusinessAccountUserSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(result))
                    .setDomainBusinessAccountUsers(domainBusinessAccountUserRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
