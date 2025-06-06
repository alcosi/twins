package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.domain.DomainBusinessAccountSearchRqDTOv1;
import org.twins.core.dto.rest.domain.DomainBusinessAccountSearchRsDTOv1;
import org.twins.core.dto.rest.domain.DomainBusinessAccountViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainBusinessAccountDTOMapper;
import org.twins.core.mappers.rest.domain.DomainBusinessAccountSearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainBusinessAccountService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.DOMAIN_BUSINESS_ACCOUNT_VIEW)
public class DomainBusinessAccountListController extends ApiController {
    private final AuthService authService;
    private final DomainBusinessAccountService domainBusinessAccountService;
    private final DomainBusinessAccountDTOMapper domainBusinessAccountDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final DomainBusinessAccountSearchRestDTOReverseMapper domainBusinessAccountSearchRestDTOReverseMapper;
    private final PaginationMapper paginationMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainBusinessAccountSearchV1", summary = "Returns domain business account search result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain business account list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainBusinessAccountSearchRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/business_account/search/v1")
    public ResponseEntity<?> domainBusinessAccountSearchV1(
            @MapperContextBinding(roots = DomainBusinessAccountDTOMapper.class, response = DomainBusinessAccountSearchRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination,
            @RequestBody DomainBusinessAccountSearchRqDTOv1 request) {
        DomainBusinessAccountSearchRsDTOv1 rs = new DomainBusinessAccountSearchRsDTOv1();
        try {
            PaginationResult<DomainBusinessAccountEntity> domainBusinessAccounts = domainBusinessAccountService.findDomainBusinessAccounts(domainBusinessAccountSearchRestDTOReverseMapper.convert(request), pagination);
            rs
                    .setBusinessAccounts(domainBusinessAccountDTOMapper.convertCollection(domainBusinessAccounts.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(domainBusinessAccounts))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "domainBusinessAccountViewV1", summary = "Returns domain business account result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain business account prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainBusinessAccountViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/domain/business_account/{businessAccountId}/v1")
    public ResponseEntity<?> domainBusinessAccountViewV1(
            @MapperContextBinding(roots = DomainBusinessAccountDTOMapper.class, response = DomainBusinessAccountViewRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.BUSINESS_ACCOUNT_ID) @PathVariable("businessAccountId") UUID businessAccountId) {
        DomainBusinessAccountViewRsDTOv1 rs = new DomainBusinessAccountViewRsDTOv1();
        try {
            DomainBusinessAccountEntity domainBusinessAccount = domainBusinessAccountService.getDomainBusinessAccountEntitySafe(authService.getApiUser().getDomainId(), businessAccountId);
            rs
                    .setBusinessAccount(domainBusinessAccountDTOMapper.convert(domainBusinessAccount, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
