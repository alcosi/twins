package org.twins.core.controller.rest.priv.domain;

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
import org.twins.core.dto.rest.domain.DomainBusinessAccountCountRqDTOv1;
import org.twins.core.dto.rest.domain.DomainBusinessAccountCountRsDTOv1;
import org.twins.core.dto.rest.domain.DomainBusinessAccountSearchRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainBusinessAccountCountRestDTOMapper;
import org.twins.core.mappers.rest.domain.DomainBusinessAccountSearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainBusinessAccountSearchService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_BUSINESS_ACCOUNT_MANAGE, Permissions.DOMAIN_BUSINESS_ACCOUNT_VIEW})
public class DomainBusinessAccountCountController extends ApiController {
    private final DomainBusinessAccountSearchService domainBusinessAccountSearchService;
    private final DomainBusinessAccountCountRestDTOMapper domainBusinessAccountCountRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final DomainBusinessAccountSearchRestDTOReverseMapper domainBusinessAccountSearchRestDTOReverseMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainBusinessAccountCountV1", summary = "Return count of domain business account grouped by specified fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain business account list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainBusinessAccountSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/business_account/count/v1")
    public ResponseEntity<?> domainBusinessAccountCountV1(
            @MapperContextBinding(roots = DomainBusinessAccountCountRestDTOMapper.class, response = DomainBusinessAccountSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody @Valid DomainBusinessAccountCountRqDTOv1 request) {
        var rs = new DomainBusinessAccountCountRsDTOv1();
        try {
            var results = domainBusinessAccountSearchService
                    .countByGroupFields(domainBusinessAccountSearchRestDTOReverseMapper.convert(request.getSearch(), mapperContext), request.getGroupFields(), pagination);
            rs
                    .setCounts(domainBusinessAccountCountRestDTOMapper.convertCollection(results.getList(), mapperContext))
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
