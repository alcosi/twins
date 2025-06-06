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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserNoDomainHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.SimplePaginationParams;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.apiuser.UserResolverAuthToken;
import org.twins.core.dto.rest.domain.DomainListRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.permission.Permissions;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.DOMAIN_VIEW)
public class DomainListController extends ApiController {
    private final AuthService authService;
    private final UserResolverAuthToken userResolverAuthToken;
    private final DomainUserService domainUserService;
    private final DomainViewRestDTOMapper domainViewRestDTOMapper;
    private final PaginationMapper paginationMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainListV1", summary = "Return a list of domains for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "domain data list", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/domain/list/v1")
    public ResponseEntity<?> domainListV1(
            @MapperContextBinding(roots = DomainViewRestDTOMapper.class, response = DomainListRsDTOv1.class) MapperContext mapperContext,
            @SimplePaginationParams SimplePagination pagination) {
        DomainListRsDTOv1 rs = new DomainListRsDTOv1();
        try {
//            authService.getApiUser()
//                    .setUserResolver(userResolverAuthToken)
//                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified())
//                    .setLocaleResolver(new LocaleResolverEnglish())//todo may throw an error
//                    .setDomainResolver(new DomainResolverNotSpecified());
            PaginationResult<DomainEntity> domainList = domainUserService
                    .findDomainListByUser(pagination);
            rs
                    .setDomains(domainViewRestDTOMapper.convertCollection(domainList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(domainList))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
