package org.twins.core.controller.rest.priv.factory;

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
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryBranchSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryBranchRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryBranchSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryBranchSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_BRANCH_MANAGE, Permissions.FACTORY_BRANCH_VIEW})
public class FactoryBranchSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryBranchSearchDTOReverseMapper factoryBranchSearchDTOReverseMapper;
    private final FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;
    private final FactoryBranchSearchService factoryBranchSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryBranchSearchV1", summary = "Factory branch search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory branch list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryBranchSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_branch/search/v1")
    public ResponseEntity<?> factoryBranchSearchV1(
            @MapperContextBinding(roots = FactoryBranchRestDTOMapper.class, response = FactoryBranchSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryBranchSearchRqDTOv1 request) {
        FactoryBranchSearchRsDTOv1 rs = new FactoryBranchSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryBranchEntity> branchList = factoryBranchSearchService
                    .search(factoryBranchSearchDTOReverseMapper.convert(request.getSearch()), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setBranches(factoryBranchRestDTOMapper.convertCollection(branchList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(branchList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
