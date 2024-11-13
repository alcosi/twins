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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserSearchRqDTOv1;
import org.twins.core.dto.rest.domain.DomainUserSearchRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainUserRestDTOMapperV2;
import org.twins.core.mappers.rest.domain.DomainUserSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainUserSearchService;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainUserSearchController extends ApiController {
    private final DomainUserSearchService domainUserSearchService;
    private final DomainUserSearchDTOReverseMapper domainUserSearchDTOReverseMapper;
    private final DomainUserRestDTOMapperV2 domainUserRestDTOMapperV2;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainUserSearchListV1", summary = "Return a list of users by current domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainUserSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/user/search/v1")
    public ResponseEntity<?> domainUserSearchListV1(
            @MapperContextBinding(roots = DomainUserRestDTOMapperV2.class, response = DomainUserSearchRsDTOv1.class) MapperContext mapperContext,
            @RequestBody DomainUserSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        DomainUserSearchRsDTOv1 rs = new DomainUserSearchRsDTOv1();
        try {
            PaginationResult<DomainUserEntity> domainUserList = domainUserSearchService
                    .findDomainUser(domainUserSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setUsers(domainUserRestDTOMapperV2.convertCollection(domainUserList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(domainUserList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
