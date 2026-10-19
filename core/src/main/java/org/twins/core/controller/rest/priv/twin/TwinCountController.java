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
import org.cambium.common.pagination.SimplePagination;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.*;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinCountRqDTOv1;
import org.twins.core.dto.rest.twin.TwinCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinCountRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinSearchExtendedDTOv2ReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinSearchServiceV2;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinCountController extends ApiController {
    private final TwinSearchServiceV2 twinSearchServiceV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinSearchExtendedDTOv2ReverseMapper twinSearchExtendedDTOv2ReverseMapper;
    private final TwinCountRestDTOMapper twinCountRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCountV1", summary = "Count twins grouped by TwinClassFieldId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count results", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/count/v1")
    @Loggable(rsBodyThreshold = 2000)
    public ResponseEntity<?> twinCountV1(
            @MapperContextBinding(roots = TwinCountRestDTOMapper.class, response = TwinCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinCountRqDTOv1 request) {
        TwinCountRsDTOv1 rs = new TwinCountRsDTOv1();
        try {
            BasicSearch search = twinSearchExtendedDTOv2ReverseMapper.convert(request.getSearch());
            var results = twinSearchServiceV2.countByGroupFields(search, request.getGroupFields(), pagination);
            rs
                    .setCounts(twinCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
