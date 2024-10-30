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
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.domain.apiuser.BusinessAccountResolverGivenId;
import org.twins.core.domain.apiuser.DomainResolverGivenId;
import org.twins.core.domain.apiuser.LocaleResolverEnglish;
import org.twins.core.domain.apiuser.UserResolverSystem;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.DomainBusinessAccountAddRqDTOv1;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainBusinessAccountAddController extends ApiController {
    private final DomainService domainService;
    private final AuthService authService;
    private final UserResolverSystem userResolverSystem;

    @ParameterChannelHeader
    @Operation(operationId = "domainBusinessAccountAddV1", summary = "Add businessAccount to domain. " +
            "If business account is not exist it will be created. Domain must be already present.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BusinessAccount was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/{domainId}/business_account/v1")
    public ResponseEntity<?> domainBusinessAccountAddV1(
            @Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId,
            @RequestBody DomainBusinessAccountAddRqDTOv1 request) {
        Response rs = new Response();
        try {
            authService.getApiUser()
                    .setDomainResolver(new DomainResolverGivenId(domainId))
                    .setBusinessAccountResolver(new BusinessAccountResolverGivenId(request.getBusinessAccountId()))
                    .setUserResolver(userResolverSystem)
                    .setLocaleResolver(new LocaleResolverEnglish())
                    .setCheckMembershipMode(false);
            domainService.addBusinessAccount(
                    domainId,
                    request.getBusinessAccountId(),
                    request.getTierId(),
                    EntitySmartService.SaveMode.ifNotPresentCreate,
                    false);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
