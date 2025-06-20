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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.apiuser.UserResolverAuthToken;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.domain.DomainViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Get domain public data", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_MANAGE, Permissions.DOMAIN_VIEW})
public class DomainViewController extends ApiController {
    private final DomainService domainService;
    private final DomainViewRestDTOMapper domainViewRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final AuthService authService;
    private final UserResolverAuthToken userResolverAuthToken;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainViewV1", summary = "Returns domain data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = " domain details prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/domain/{domainId}/v1")
    @Loggable(rsBodyThreshold = 1000)
    public ResponseEntity<?> domainViewV1(
            @MapperContextBinding(roots = DomainViewRestDTOMapper.class, response = DomainViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId) {

        DomainViewRsDTOv1 rs = new DomainViewRsDTOv1();
        try {
//            authService.getApiUser()
//                    .setUserResolver(userResolverAuthToken)
//                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified())
//                    .setLocaleResolver(new LocaleResolverEnglish())
//                    .setDomainResolver(new DomainResolverNotSpecified());
            DomainEntity domainEntity = domainService.findEntitySafe(domainId);
            rs
                    .setDomain(domainViewRestDTOMapper.convert(domainEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


}
