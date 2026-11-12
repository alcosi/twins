package org.twins.core.controller.rest.priv.domainversion;

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
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.search.DomainVersionSearch;
import org.twins.core.dto.rest.domainversion.DomainVersionSearchRqDTOv1;
import org.twins.core.dto.rest.domainversion.DomainVersionSearchRsDTOv1;
import org.twins.core.mappers.rest.domainversion.DomainVersionRestDTOMapper;
import org.twins.core.mappers.rest.domainversion.DomainVersionSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainVersionSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DOMAIN_VERSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_VERSION_MANAGE, Permissions.DOMAIN_VERSION_VIEW})
public class DomainVersionSearchController extends ApiController {
    private final DomainVersionSearchService domainVersionSearchService;
    private final DomainVersionRestDTOMapper domainVersionRestDTOMapper;
    private final DomainVersionSearchDTOReverseMapper domainVersionSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainVersionSearchV1", summary = "Domain version search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain version list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainVersionSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain_version/search/v1")
    public ResponseEntity<?> domainVersionSearchV1(
            @MapperContextBinding(roots = DomainVersionRestDTOMapper.class, response = DomainVersionSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainVersionSearchRqDTOv1 request) {
        DomainVersionSearchRsDTOv1 rs = new DomainVersionSearchRsDTOv1();
        try {
            DomainVersionSearch search = domainVersionSearchDTOReverseMapper.convert(request.getSearch());
            PaginationResult<DomainVersionEntity> result =
                    domainVersionSearchService.search(search, pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(result))
                    .setDomainVersions(domainVersionRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
