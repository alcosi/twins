package org.twins.core.controller.rest.priv.tier;

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
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.domain.search.TierSearch;
import org.twins.core.dto.rest.tier.TierSearchRqDTOv1;
import org.twins.core.dto.rest.tier.TierSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.mappers.rest.tier.TierSearchDTOReverseMapper;
import org.twins.core.service.domain.TierSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.TIER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TIER_MANAGE, Permissions.TIER_VIEW})
public class TierSearchController extends ApiController {

    private final TierSearchService tierSearchService;
    private final TierSearchDTOReverseMapper tierSearchDTOReverseMapper;
    private final TierRestDTOMapper tierRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "tierSearchV1", summary = "Return a list of tiers by search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TierSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/tier/search/v1")
    public ResponseEntity<?> tierSearchV1(
            @MapperContextBinding(roots = TierRestDTOMapper.class, response = TierSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TierSearchRqDTOv1 request,
            @SimplePaginationParams SimplePagination pagination) {

        TierSearchRsDTOv1 rs = new TierSearchRsDTOv1();
        try {
            TierSearch tierSearch = tierSearchDTOReverseMapper.convert(request);

            PaginationResult<TierEntity> tierList = tierSearchService.findTiers(tierSearch, pagination);

            rs.setPagination(paginationMapper.convert(tierList))
                    .setTiers(tierRestDTOMapper.convertCollection(tierList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}