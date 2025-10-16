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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.domain.DomainSubscriptionEventService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_SUBSCRIPTION_EVENT_MANAGE, Permissions.DOMAIN_SUBSCRIPTION_EVENT_DELETE})
public class DomainSubscriptionEventDeleteController extends ApiController {

    private final DomainSubscriptionEventService domainSubscriptionEventService;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainSubscriptionEventDeleteV1", summary = "Delete domain subscription event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain subscription event was deleted successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Response.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @DeleteMapping(value = "/private/domain/subscription_event/{domainSubscriptionEventId}/v1")
    public ResponseEntity<?> domainSubscriptionEventDeleteV1(@Parameter(example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainSubscriptionEventId) {
        Response rs = new Response();

        try {
            domainSubscriptionEventService.deleteDomainSubscriptionEvent(domainSubscriptionEventId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}