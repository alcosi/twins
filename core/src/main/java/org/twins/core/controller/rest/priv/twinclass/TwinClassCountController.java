package org.twins.core.controller.rest.priv.twinclass;

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
import org.twins.core.dto.rest.twinclass.TwinClassCountRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassCountRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassCountRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassSearchRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassSearchService;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassCountController extends ApiController {
    private final TwinClassSearchService twinClassSearchService;
    private final TwinClassCountRestDTOMapper twinClassCountRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinClassSearchRestDTOReverseMapper twinClassSearchRestDTOReverseMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassCountV1", summary = "Returns twin class count grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class count prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/count/v1")
    public ResponseEntity<?> twinClassCountV1(
            @MapperContextBinding(roots = TwinClassCountRestDTOMapper.class, response = TwinClassCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinClassCountRqDTOv1 request) {
        TwinClassCountRsDTOv1 rs = new TwinClassCountRsDTOv1();
        try {
            var results = twinClassSearchService
                    .countByGroupFields(twinClassSearchRestDTOReverseMapper.convert(request.getSearch()), request.getGroupFields(), pagination);
            rs
                    .setCounts(twinClassCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
