package org.twins.core.controller.rest.priv.link;

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
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dto.rest.twin.TwinLinkListRsDTOv1;
import org.twins.core.dto.rest.twin.TwinLinkSearchRqDTOv1;
import org.twins.core.mappers.rest.link.TwinLinkBaseRestDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_VIEW})
public class TwinLinkSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinLinkSearchDTOReverseMapper twinLinkSearchDTOReverseMapper;
    private final TwinLinkBaseRestDTOMapper twinLinkBaseRestDTOMapper;
    private final TwinLinkService twinLinkService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinLinkSearchV1", summary = "Twin link search data list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin link data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinLinkListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_link/search/v1")
    public ResponseEntity<?> twinLinkSearchV1(
            @MapperContextBinding(roots = TwinLinkBaseRestDTOMapper.class, response = TwinLinkListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody TwinLinkSearchRqDTOv1 request) {
        TwinLinkListRsDTOv1 rs = new TwinLinkListRsDTOv1();
        try {
            PaginationResult<TwinLinkEntity> twinLinkList = twinLinkService
                    .findTwinLinks(twinLinkSearchDTOReverseMapper.convert(request.getTwinkLinkSearch()), pagination);
            rs
                    .setTwinLinks(twinLinkBaseRestDTOMapper.convertCollection(twinLinkList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(twinLinkList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
