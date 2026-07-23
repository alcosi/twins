package org.twins.core.controller.rest.priv.link;

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
import org.twins.core.dto.rest.link.TwinLinkCountRqDTOv1;
import org.twins.core.dto.rest.link.TwinLinkCountRsDTOv1;
import org.twins.core.mappers.rest.link.TwinLinkCountRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.TwinLinkSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_LINK_MANAGE, Permissions.TWIN_LINK_VIEW})
public class TwinLinkCountController extends ApiController {
    private final TwinLinkSearchService twinLinkSearchService;
    private final TwinLinkSearchDTOReverseMapper twinLinkSearchDTOReverseMapper;
    private final TwinLinkCountRestDTOMapper twinLinkCountRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinLinkCountV1", summary = "Return count of twin links grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinLinkCountRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_link/search/count/v1")
    public ResponseEntity<?> twinLinkCountV1(
            @MapperContextBinding(roots = TwinLinkCountRestDTOMapper.class, response = TwinLinkCountRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid TwinLinkCountRqDTOv1 request) {
        TwinLinkCountRsDTOv1 rs = new TwinLinkCountRsDTOv1();
        try {
            var results =
                    twinLinkSearchService.countByGroupFields(twinLinkSearchDTOReverseMapper
                            .convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(twinLinkCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
