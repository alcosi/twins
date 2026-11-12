package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twin.TwinGroupCriterion;
import org.twins.core.domain.twin.TwinGroupedSearch;
import org.twins.core.domain.twin.TwinGroupedSearchResult;
import org.twins.core.domain.twin.TwinMemberSort;
import org.twins.core.dto.rest.twin.TwinGroupCriterionDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchRsDTOv1;
import org.twins.core.dto.rest.twin.TwinSortDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinGroupedSearchGroupRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinSearchDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinGroupedSearchService;

import java.util.ArrayList;
import java.util.List;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinGroupedSearchController extends ApiController {
    private final TwinGroupedSearchService twinGroupedSearchService;
    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;
    private final TwinGroupedSearchGroupRestDTOMapper twinGroupedSearchGroupRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinGroupedSearchV1", summary = "Search twins of one class and fold them into a tree of up to 3 field or link criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grouped twins", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinGroupedSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/search/grouped/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinGroupedSearchV1(
            @MapperContextBinding(roots = {TwinGroupedSearchGroupRestDTOMapper.class, TwinRestDTOMapperV2.class}, response = TwinGroupedSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid TwinGroupedSearchRqDTOv1 request) {
        TwinGroupedSearchRsDTOv1 rs = new TwinGroupedSearchRsDTOv1();
        try {
            BasicSearch search = request.getSearch() == null ? null : twinSearchDTOReverseMapper.convert(request.getSearch());
            TwinGroupedSearchResult result = twinGroupedSearchService.search(new TwinGroupedSearch()
                    .setSearch(search)
                    .setCriteria(criteria(request.getCriteria()))
                    .setMemberSorts(memberSorts(request.getMemberSorts())));
            rs
                    .setGroups(twinGroupedSearchGroupRestDTOMapper.convertCollection(result.getGroups(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    private static List<TwinGroupCriterion> criteria(List<TwinGroupCriterionDTOv1> criteria) {
        if (criteria == null) {
            return null;
        }
        List<TwinGroupCriterion> result = new ArrayList<>(criteria.size());
        for (TwinGroupCriterionDTOv1 criterion : criteria) {
            if (criterion == null) {
                result.add(null);
                continue;
            }
            result.add(new TwinGroupCriterion()
                    .setType(criterion.getType())
                    .setFieldId(criterion.getFieldId())
                    .setLinkId(criterion.getLinkId())
                    .setNodeEnd(criterion.getNodeEnd())
                    .setSort(criterion.getSort()));
        }
        return result;
    }

    private static List<TwinMemberSort> memberSorts(List<TwinSortDTOv1> sorts) {
        if (sorts == null) {
            return null;
        }
        List<TwinMemberSort> memberSorts = new ArrayList<>(sorts.size());
        for (TwinSortDTOv1 sort : sorts) {
            if (sort == null) {
                memberSorts.add(null);
                continue;
            }
            memberSorts.add(new TwinMemberSort()
                    .setFieldId(sort.getTwinClassFieldId())
                    .setDirection(sort.getDirection()));
        }
        return memberSorts;
    }
}
