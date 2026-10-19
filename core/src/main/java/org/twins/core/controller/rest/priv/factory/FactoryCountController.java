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
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryCountRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryCountRsDTOv1;
import org.twins.core.enums.sort.FactoryGroupField;
import org.twins.core.mappers.rest.factory.FactoryCountRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactorySearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.factory.FactorySearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.FACTORY)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.FACTORY_MANAGE, Permissions.FACTORY_VIEW})
public class FactoryCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final FactorySearchDTOReverseMapper factorySearchDTOReverseMapper;
    private final FactoryCountRestDTOMapper factoryCountRestDTOMapper;
    private final FactorySearchService factorySearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "factoryCountV1", summary = "Count factories by group fields with optional search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = FactoryCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/factory/count/v1")
    public ResponseEntity<?> factoryCountV1(
            @MapperContextBinding(roots = FactoryCountRestDTOMapper.class, response = FactoryCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody FactoryCountRqDTOv1 request) {
        FactoryCountRsDTOv1 rs = new FactoryCountRsDTOv1();
        try {
            PaginationResult<CountResult<TwinFactoryEntity, FactoryGroupField>> result = factorySearchService
                    .countByGroupFields(factorySearchDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(factoryCountRestDTOMapper.convertCollection(result.getList(), mapperContext))
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
