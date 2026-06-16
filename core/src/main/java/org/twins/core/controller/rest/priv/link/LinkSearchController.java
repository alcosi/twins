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
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.link.LinkSearchRqDTOv1;
import org.twins.core.dto.rest.link.LinkSearchRqDTOv2;
import org.twins.core.dto.rest.link.LinkSearchRsDTOv1;
import org.twins.core.dto.rest.link.LinkSearchRsDTOv2;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOV2Mapper;
import org.twins.core.mappers.rest.link.LinkSearchDTOReverseMapper;
import org.twins.core.mappers.rest.link.LinkSearchRqDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkSearchService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.permission.Permissions;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.LINK_MANAGE, Permissions.LINK_VIEW})
public class LinkSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final LinkForwardRestDTOV2Mapper linkForwardRestDTOV2Mapper;
    private final LinkSearchRqDTOReverseMapper linkSearchRqDTOReverseMapper;
    private final LinkSearchDTOReverseMapper linkSearchDTOReverseMapper;
    private final LinkSearchService linkSearchService;
    private final LinkService linkService;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "linkSearchV1", summary = "Link search (deprecated — use v2)",
            description = "Deprecated. Use linkSearchV2 — supports dynamic sort via sortField/sortDirection.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link data list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/link/search/v1")
    public ResponseEntity<?> linkSearchV1(
            @MapperContextBinding(roots = LinkForwardRestDTOV2Mapper.class, response = LinkSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody LinkSearchRqDTOv1 request) {
        LinkSearchRsDTOv1 rs = new LinkSearchRsDTOv1();
        try {
            PaginationResult<LinkEntity> linkList = linkSearchService
                    .findLinks(linkSearchRqDTOReverseMapper.convert(request), pagination);
            rs
                    .setLinks(linkForwardRestDTOV2Mapper.convertCollection(linkList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(linkList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "linkSearchV2", summary = "Link search with dynamic sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link data list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkSearchRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/link/search/v2")
    public ResponseEntity<?> linkSearchV2(
            @MapperContextBinding(roots = LinkForwardRestDTOV2Mapper.class, response = LinkSearchRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody LinkSearchRqDTOv2 request) {
        LinkSearchRsDTOv2 rs = new LinkSearchRsDTOv2();
        try {
            PaginationResult<LinkEntity> linkList = linkSearchService
                    .search(linkSearchDTOReverseMapper.convert(request.getSearch()), pagination,
                            request.getSortField(), request.getSortDirection());
            rs
                    .setLinks(linkForwardRestDTOV2Mapper.convertCollection(linkList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(linkList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
