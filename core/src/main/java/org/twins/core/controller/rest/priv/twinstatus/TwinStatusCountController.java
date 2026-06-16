package org.twins.core.controller.rest.priv.twinstatus;

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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dto.rest.twinstatus.TwinStatusCountRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusCountRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusSearchService;

@Tag(name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_STATUS_MANAGE, Permissions.TWIN_STATUS_VIEW})
public class TwinStatusCountController extends ApiController {
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final TwinStatusSearchRestDTOReverseMapper twinStatusSearchRestDTOReverseMapper;
    private final TwinStatusCountRestDTOMapper twinStatusCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final TwinStatusSearchService twinStatusSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusCountV1", summary = "Returns twin status count grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status count prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/count/v1")
    public ResponseEntity<?> twinStatusCountV1(
            @MapperContextBinding(roots = TwinStatusCountRestDTOMapper.class, response = TwinStatusCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinStatusCountRqDTOv1 request) {
        TwinStatusCountRsDTOv1 rs = new TwinStatusCountRsDTOv1();
        try {
            var results = twinStatusSearchService
                    .countByGroupFields(twinStatusSearchRestDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(twinStatusCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(results))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
