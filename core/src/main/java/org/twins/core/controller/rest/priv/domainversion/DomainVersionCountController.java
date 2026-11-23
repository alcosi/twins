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
import org.twins.core.domain.CountResult;
import org.twins.core.domain.search.DomainVersionSearch;
import org.twins.core.dto.rest.domainversion.DomainVersionCountRqDTOv1;
import org.twins.core.dto.rest.domainversion.DomainVersionCountRsDTOv1;
import org.twins.core.enums.sort.DomainVersionGroupField;
import org.twins.core.mappers.rest.domainversion.DomainVersionCountRestDTOMapper;
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
public class DomainVersionCountController extends ApiController {
    private final DomainVersionSearchService domainVersionSearchService;
    private final DomainVersionCountRestDTOMapper domainVersionCountRestDTOMapper;
    private final DomainVersionSearchDTOReverseMapper domainVersionSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainVersionCountV1", summary = "Count domain versions by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain version count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainVersionCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain_version/count/v1")
    public ResponseEntity<?> domainVersionCountV1(
            @MapperContextBinding(roots = DomainVersionCountRestDTOMapper.class, response = DomainVersionCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainVersionCountRqDTOv1 request) {
        DomainVersionCountRsDTOv1 rs = new DomainVersionCountRsDTOv1();
        try {
            DomainVersionSearch search = domainVersionSearchDTOReverseMapper.convert(request.getSearch());
            PaginationResult<CountResult<DomainVersionEntity, DomainVersionGroupField>> result =
                    domainVersionSearchService.countByGroupFields(search, request.getGroupFields(), pagination);
            rs
                    .setCounts(domainVersionCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
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
