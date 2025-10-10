package org.twins.core.controller.rest.priv.factory;

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
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryBranchSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryBranchSearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryBranchViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryBranchRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryBranchSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryBranchSearchService;
import org.twins.core.service.factory.FactoryBranchService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.BRANCH_MANAGE, Permissions.BRANCH_VIEW})
public class FactoryBranchSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryBranchSearchDTOReverseMapper factoryBranchSearchDTOReverseMapper;
    private final FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;
    private final FactoryBranchSearchService factoryBranchSearchService;
    private final FactoryBranchService factoryBranchService;
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
                    .findFactoryBranches(factoryBranchSearchDTOReverseMapper.convert(request), pagination);
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

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryBranchViewV1", summary = "Factory branch by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory branch data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryBranchViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_branch/{factoryBranchId}/v1")
    public ResponseEntity<?> factoryBranchViewV1(
            @MapperContextBinding(roots = FactoryBranchRestDTOMapper.class, response = FactoryBranchViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_BRANCH_ID) @PathVariable("factoryBranchId")UUID factoryBranchId) {
        FactoryBranchViewRsDTOv1 rs = new FactoryBranchViewRsDTOv1();
        try {
            TwinFactoryBranchEntity branch = factoryBranchService.findEntitySafe(factoryBranchId);

            rs
                    .setBranch(factoryBranchRestDTOMapper.convert(branch, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
