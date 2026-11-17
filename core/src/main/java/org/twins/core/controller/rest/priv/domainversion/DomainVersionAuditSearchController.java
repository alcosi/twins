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
import org.twins.core.dao.domain.DomainVersionAuditEntity;
import org.twins.core.domain.search.DomainVersionAuditSearch;
import org.twins.core.dto.rest.domainversion.DomainVersionAuditSearchRqDTOv1;
import org.twins.core.dto.rest.domainversion.DomainVersionAuditSearchRsDTOv1;
import org.twins.core.mappers.rest.domainversion.DomainVersionAuditRestDTOMapper;
import org.twins.core.mappers.rest.domainversion.DomainVersionAuditSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainVersionAuditSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.DOMAIN_VERSION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_VERSION_AUDIT_MANAGE, Permissions.DOMAIN_VERSION_AUDIT_VIEW})
public class DomainVersionAuditSearchController extends ApiController {
    private final DomainVersionAuditSearchService domainVersionAuditSearchService;
    private final DomainVersionAuditRestDTOMapper domainVersionAuditRestDTOMapper;
    private final DomainVersionAuditSearchDTOReverseMapper domainVersionAuditSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainVersionAuditSearchV1", summary = "Domain version audit search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain version audit list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainVersionAuditSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain_version_audit/search/v1")
    public ResponseEntity<?> domainVersionAuditSearchV1(
            @MapperContextBinding(roots = DomainVersionAuditRestDTOMapper.class, response = DomainVersionAuditSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainVersionAuditSearchRqDTOv1 request) {
        DomainVersionAuditSearchRsDTOv1 rs = new DomainVersionAuditSearchRsDTOv1();
        try {
            DomainVersionAuditSearch search = domainVersionAuditSearchDTOReverseMapper.convert(request.getSearch());
            PaginationResult<DomainVersionAuditEntity> result =
                    domainVersionAuditSearchService.search(search, pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(result))
                    .setDomainVersionAudits(domainVersionAuditRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
