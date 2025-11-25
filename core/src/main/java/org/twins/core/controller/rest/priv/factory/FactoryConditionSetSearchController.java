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
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryConditionSetSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetSearchRsDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetViewRsDTOv1;
import org.twins.core.mappers.rest.factory.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionSetSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactoryConditionSetSearchService;
import org.twins.core.service.factory.FactoryConditionSetService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.CONDITION_SET_MANAGE, Permissions.CONDITION_SET_VIEW})
public class FactoryConditionSetSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final FactoryConditionSetSearchRqDTOReverseMapper factoryConditionSetSearchRqDTOReverseMapper;
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;
    private final FactoryConditionSetSearchService factoryConditionSetSearchService;
    private final FactoryConditionSetService factoryConditionSetService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionSetSearchV1", summary = "Condition set search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Condition set list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryConditionSetSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory_condition_set/search/v1")
    public ResponseEntity<?> factoryConditionSetSearchV1(
            @MapperContextBinding(roots = FactoryConditionSetRestDTOMapper.class, response = FactoryConditionSetSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryConditionSetSearchRqDTOv1 request) {
        FactoryConditionSetSearchRsDTOv1 rs = new FactoryConditionSetSearchRsDTOv1();
        try {
            PaginationResult<TwinFactoryConditionSetEntity> conditionSetList = factoryConditionSetSearchService
                    .findFactoryConditionSets(factoryConditionSetSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setPagination(paginationMapper.convert(conditionSetList))
                    .setConditionSets(factoryConditionSetRestDTOMapper.convertCollection(conditionSetList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryConditionSetViewV1", summary = "Condition set view by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Condition set", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryConditionSetViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/factory_condition_set/{factoryConditionSetId}/v1")
    public ResponseEntity<?> factoryConditionSetViewV1(
            @MapperContextBinding(roots = FactoryConditionSetRestDTOMapper.class, response = FactoryConditionSetViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.FACTORY_CONDITION_SET_ID) @PathVariable("factoryConditionSetId") UUID factoryConditionSetId) {
        FactoryConditionSetViewRsDTOv1 rs = new FactoryConditionSetViewRsDTOv1();
        try {
            TwinFactoryConditionSetEntity conditionSet = factoryConditionSetService.findEntitySafe(factoryConditionSetId);
            rs
                    .setConditionSet(factoryConditionSetRestDTOMapper.convert(conditionSet, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
