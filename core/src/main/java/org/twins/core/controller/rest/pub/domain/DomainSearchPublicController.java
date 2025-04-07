package org.twins.core.controller.rest.pub.domain;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainPublicSearchRqDTOv1;
import org.twins.core.dto.rest.domain.DomainPublicSearchRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainSearchDTOReverseMapper;
import org.twins.core.mappers.rest.domain.DomainViewPublicRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainSearchService;


@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainSearchPublicController extends ApiController {
    private final DomainSearchService domainPublicSearchService;
    private final DomainSearchDTOReverseMapper domainSearchDTOReverseMapper;
    private final DomainViewPublicRestDTOMapper domainViewRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @Operation(operationId = "domainSearchPublicV1", summary = "Search public domain data by key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public domain details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainPublicSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/public/domain/search/v1")
    public ResponseEntity<?> domainSearchPublicV1(
            @MapperContextBinding(roots = DomainViewPublicRestDTOMapper.class, response = DomainPublicSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainPublicSearchRqDTOv1 request) {
        DomainPublicSearchRsDTOv1 rs = new DomainPublicSearchRsDTOv1();
        try {
            PaginationResult<DomainEntity> domainList = domainPublicSearchService
                    .findDomains(domainSearchDTOReverseMapper.convert(request.getDomainPublicSearch()), pagination);

            rs
                    .setDomains(domainViewRestDTOMapper.convertCollection(domainList.getList(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
