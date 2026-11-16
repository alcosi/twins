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
import org.twins.core.domain.twin.TwinGroupedSearchResult;
import org.twins.core.dto.rest.twin.TwinGroupedSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinGroupedSearchDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinGroupedSearchGroupRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinGroupedSearchService;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinGroupedSearchController extends ApiController {
    private final TwinGroupedSearchService twinGroupedSearchService;
    private final TwinGroupedSearchDTOReverseMapper twinGroupedSearchDTOReverseMapper;
    private final TwinGroupedSearchGroupRestDTOMapper twinGroupedSearchGroupRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinGroupedSearchV1", summary = "Search twins of one class and fold them into a tree of up to 3 levels")
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
            TwinGroupedSearchResult result = twinGroupedSearchService.search(twinGroupedSearchDTOReverseMapper.convert(request));
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
}
