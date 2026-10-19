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
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryBranchCountRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryBranchCountRsDTOv1;
import org.twins.core.enums.sort.FactoryBranchGroupField;
import org.twins.core.mappers.rest.factory.FactoryBranchCountRestDTOMapper;
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
public class FactoryBranchCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactoryBranchSearchDTOReverseMapper factoryBranchSearchDTOReverseMapper;
    private final FactoryBranchCountRestDTOMapper factoryBranchCountRestDTOMapper;
    private final FactoryBranchSearchService factoryBranchSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryBranchCountV1", summary = "Count factory branches by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory branch count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryBranchCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_branch/count/v1")
    public ResponseEntity<?> factoryBranchCountV1(
            @MapperContextBinding(roots = FactoryBranchCountRestDTOMapper.class, response = FactoryBranchCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryBranchCountRqDTOv1 request) {
        FactoryBranchCountRsDTOv1 rs = new FactoryBranchCountRsDTOv1();
        try {
            PaginationResult<CountResult<TwinFactoryBranchEntity, FactoryBranchGroupField>> result = factoryBranchSearchService
                    .countByGroupFields(factoryBranchSearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(factoryBranchCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(result))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
