package org.twins.core.controller.rest.priv.twinflow;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerSearchRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinFactoryTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinFactoryTriggerSearchDTOReverseMapper;
import org.twins.core.service.factory.TwinFactoryTriggerSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_TRIGGER_MANAGE, Permissions.TWIN_TRIGGER_VIEW})
public class TwinFactoryTriggerSearchController extends ApiController {
    private final TwinFactoryTriggerSearchService twinFactoryTriggerSearchService;
    private final TwinFactoryTriggerRestDTOMapper twinFactoryTriggerRestDTOMapper;
    private final TwinFactoryTriggerSearchDTOReverseMapper twinFactoryTriggerSearchDTOReverseMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinFactoryTriggerSearchV1", summary = "Search twin factory triggers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinFactoryTriggerSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_factory/trigger/search/v1")
    public ResponseEntity<?> twinFactoryTriggerSearchV1(
            @MapperContextBinding(roots = TwinFactoryTriggerRestDTOMapper.class, response = TwinFactoryTriggerSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinFactoryTriggerSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {
        TwinFactoryTriggerSearchRsDTOv1 rs = new TwinFactoryTriggerSearchRsDTOv1();
        try {
            PaginationResult<org.twins.core.dao.factory.TwinFactoryTriggerEntity> factoryTriggerList = twinFactoryTriggerSearchService
                    .findFactoryTriggers(twinFactoryTriggerSearchDTOReverseMapper.convert(request.getSearch()), pagination);
            rs
                    .setPagination(paginationMapper.convert(factoryTriggerList))
                    .setTwinFactoryTriggers(twinFactoryTriggerRestDTOMapper.convertCollection(factoryTriggerList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
