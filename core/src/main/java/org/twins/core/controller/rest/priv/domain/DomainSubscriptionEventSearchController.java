package org.twins.core.controller.rest.priv.domain;

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
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventSearchRqDTOv1;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventSearchRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainSubscriptionEventBaseRestDTOMapper;
import org.twins.core.mappers.rest.domain.DomainSubscriptionEventSearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainSubscriptionEventSearchService;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
//@ProtectedBy({Permissions.DOMAIN_SUBSCRIPTION_EVENT_MANAGE, Permissions.DOMAIN_SUBSCRIPTION_EVENT_VIEW})
public class DomainSubscriptionEventSearchController extends ApiController {

    private final DomainSubscriptionEventSearchRestDTOReverseMapper domainSubscriptionEventSearchRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final DomainSubscriptionEventSearchService domainSubscriptionEventSearchService;
    private final DomainSubscriptionEventBaseRestDTOMapper domainSubscriptionEventBaseRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainSubscriptionEventSearchV1", summary = "Returns domain subscription event search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain subscription event list prepared", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DomainSubscriptionEventSearchRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/subscription_event/search/v1")
    public ResponseEntity<?> domainSubscriptionEventSearchV1(
            @MapperContextBinding(roots = DomainSubscriptionEventBaseRestDTOMapper.class, response = DomainSubscriptionEventSearchRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainSubscriptionEventSearchRqDTOv1 request
    ) {
        DomainSubscriptionEventSearchRsDTOv1 rs = new DomainSubscriptionEventSearchRsDTOv1();

        try {
            PaginationResult<DomainSubscriptionEventEntity> domainSubscriptionEventList = domainSubscriptionEventSearchService.findDomainSubscriptionEvent(domainSubscriptionEventSearchRestDTOReverseMapper.convert(request), pagination);

            rs
                    .setDomainSubscriptionEvents(domainSubscriptionEventBaseRestDTOMapper.convertCollection(domainSubscriptionEventList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(domainSubscriptionEventList))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}

