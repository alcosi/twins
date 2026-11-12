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
import org.twins.core.domain.CountResult;
import org.twins.core.domain.search.DomainVersionAuditSearch;
import org.twins.core.dto.rest.domainversion.DomainVersionAuditCountRqDTOv1;
import org.twins.core.dto.rest.domainversion.DomainVersionAuditCountRsDTOv1;
import org.twins.core.enums.sort.DomainVersionAuditGroupField;
import org.twins.core.mappers.rest.domainversion.DomainVersionAuditCountRestDTOMapper;
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
public class DomainVersionAuditCountController extends ApiController {
    private final DomainVersionAuditSearchService domainVersionAuditSearchService;
    private final DomainVersionAuditCountRestDTOMapper domainVersionAuditCountRestDTOMapper;
    private final DomainVersionAuditSearchDTOReverseMapper domainVersionAuditSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainVersionAuditCountV1", summary = "Count domain version audit records by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain version audit count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainVersionAuditCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain_version_audit/count/v1")
    public ResponseEntity<?> domainVersionAuditCountV1(
            @MapperContextBinding(roots = DomainVersionAuditCountRestDTOMapper.class, response = DomainVersionAuditCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainVersionAuditCountRqDTOv1 request) {
        DomainVersionAuditCountRsDTOv1 rs = new DomainVersionAuditCountRsDTOv1();
        try {
            DomainVersionAuditSearch search = domainVersionAuditSearchDTOReverseMapper.convert(request.getSearch());
            PaginationResult<CountResult<DomainVersionAuditEntity, DomainVersionAuditGroupField>> result =
                    domainVersionAuditSearchService.countByGroupFields(search, request.getGroupFields(), pagination);
            rs
                    .setCounts(domainVersionAuditCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(result))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
