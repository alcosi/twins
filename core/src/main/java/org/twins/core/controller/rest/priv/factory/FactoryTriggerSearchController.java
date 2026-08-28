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
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.factory.FactoryTriggerSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryTriggerSearchRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryTriggerRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryTriggerSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryTriggerSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_VIEW})
public class FactoryTriggerSearchController extends ApiController {
    private final FactoryTriggerSearchService factoryTriggerSearchService;
    private final FactoryTriggerRestDTOMapper factoryTriggerRestDTOMapper;
    private final FactoryTriggerSearchDTOReverseMapper factoryTriggerSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFactoryTriggerSearchV1", summary = "Search twin factory triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryTriggerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_factory/trigger/search/v1")
    public ResponseEntity<?> twinFactoryTriggerSearchV1(
            @MapperContextBinding(roots = FactoryTriggerRestDTOMapper.class, response = FactoryTriggerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody FactoryTriggerSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        FactoryTriggerSearchRsDTOv1 rs = new FactoryTriggerSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryTriggerEntity> factoryTriggerList = factoryTriggerSearchService
                    .search(factoryTriggerSearchDTOReverseMapper.convert(request.getSearch(), mapperContext), pagination, request.getSortField(), request.getSortDirection());
            rs
                    .setPagination(paginationMapper.convert(factoryTriggerList))
                    .setFactoryTriggerList(factoryTriggerRestDTOMapper.convertCollection(factoryTriggerList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
