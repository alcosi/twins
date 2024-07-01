package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserNoDomainHeaders;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.apiuser.*;
import org.twins.core.dto.rest.domain.DomainListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.mappers.rest.pagination.PaginationMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.pagination.PageableResult;

import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_LIMIT;
import static org.cambium.common.util.PaginationUtils.DEFAULT_VALUE_OFFSET;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainListController extends ApiController {
    final AuthService authService;
    final UserResolverAuthToken userResolverAuthToken;
    final DomainService domainService;
    final DomainViewRestDTOMapper domainViewRestDTOMapper;
    final PaginationMapper paginationMapper;

    @ParametersApiUserNoDomainHeaders
    @Operation(operationId = "domainListV1", summary = "Return a list of domains for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/domain/list/v1")
    public ResponseEntity<?> domainListV1(
            @MapperContextBinding(roots = DomainViewRestDTOMapper.class, lazySupport = false) MapperContext mapperContext,
            @RequestParam(name = RestRequestParam.paginationOffset, defaultValue = DEFAULT_VALUE_OFFSET) int offset,
            @RequestParam(name = RestRequestParam.paginationLimit, defaultValue = DEFAULT_VALUE_LIMIT) int limit) {
        DomainListRsDTOv1 rs = new DomainListRsDTOv1();
        try {
            authService.getApiUser()
                    .setUserResolver(userResolverAuthToken)
                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified())
                    .setLocaleResolver(new LocaleResolverEnglish());//todo may throw an error
            PageableResult<DomainEntity> domainList = domainService.findDomainListByUser(offset, limit);
            rs
                    .setDomainList(domainViewRestDTOMapper.convertCollection(domainList.getList(), mapperContext))
                    .setPagination(paginationMapper.convert(domainList));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
