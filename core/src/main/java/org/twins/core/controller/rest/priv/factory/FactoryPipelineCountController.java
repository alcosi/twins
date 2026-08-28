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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryPipelineCountRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineCountRsDTOv1;
import org.twins.core.enums.sort.FactoryPipelineGroupField;
import org.twins.core.mappers.rest.factory.FactoryPipelineCountRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryPipelineSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryPipelineSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_PIPELINE_MANAGE, Permissions.FACTORY_PIPELINE_VIEW})
public class FactoryPipelineCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactoryPipelineSearchDTOReverseMapper factoryPipelineSearchDTOReverseMapper;
    private final FactoryPipelineCountRestDTOMapper factoryPipelineCountRestDTOMapper;
    private final FactoryPipelineSearchService factoryPipelineSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryPipelineCountV1", summary = "Count factory pipelines by group fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factory pipeline count", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryPipelineCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_pipeline/count/v1")
    public ResponseEntity<?> factoryPipelineCountV1(
            @MapperContextBinding(roots = FactoryPipelineCountRestDTOMapper.class, response = FactoryPipelineCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryPipelineCountRqDTOv1 request) {
        FactoryPipelineCountRsDTOv1 rs = new FactoryPipelineCountRsDTOv1();
        try {
            PaginationResult<CountResult<TwinFactoryPipelineEntity, FactoryPipelineGroupField>> result = factoryPipelineSearchService
                    .countByGroupFields(factoryPipelineSearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(factoryPipelineCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
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
