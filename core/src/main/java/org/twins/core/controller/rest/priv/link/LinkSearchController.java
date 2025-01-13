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
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dto.rest.factory.LinkSearchRsDTOv1;
import org.twins.core.dto.rest.link.LinkSearchRqDTOv1;
import org.twins.core.mappers.rest.link.LinkForwardRestDTOV3Mapper;
import org.twins.core.mappers.rest.link.LinkSearchDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.link.LinkSearchService;

@Tag(name = ApiTag.LINK)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class LinkSearchController extends ApiController {
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final LinkForwardRestDTOV3Mapper linkForwardRestDTOV3Mapper;
    private final LinkSearchDTOReverseMapper linkSearchDTOReverseMapper;
    private final LinkSearchService linkSearchService;

    @ParametersApiUserHeaders
    @Operation(operationId = "linkSearchV1", summary = "Link search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link data list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = LinkSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/link/search/v1")
    public ResponseEntity<?> linkSearchV1(
            @MapperContextBinding(roots = LinkForwardRestDTOV3Mapper.class, response = LinkSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody LinkSearchRqDTOv1 request) {
        LinkSearchRsDTOv1 rs = new LinkSearchRsDTOv1();
        try {
            PaginationResult<LinkEntity> linkList = linkSearchService
                    .findLinks(linkSearchDTOReverseMapper.convert(request), pagination);
            rs
                    .setLinks(linkForwardRestDTOV3Mapper.convertCollection(linkList.getList(), mapperContext))
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