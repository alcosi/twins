package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.UUIDCheckService;
import org.twins.core.service.domain.DomainService;

import java.util.UUID;

@Tag(description = "", name = "domain")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainBusinessAccountDeleteController extends ApiController {
    private final DomainService domainService;

    @Operation(operationId = "domainBusinessAccountDeleteV1", summary = "Delete businessAccount from domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BusinessAccount was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/domain/{domainId}/business_account/{businessAccountId}/v1", method = RequestMethod.DELETE)
    public ResponseEntity<?> domainBusinessAccountDeleteV1(
            @Parameter(name = "channel", in = ParameterIn.HEADER, required = true, example = DTOExamples.CHANNEL) String channel,
            @Parameter(name = "domainId", in = ParameterIn.PATH, required = true, example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId,
            @Parameter(name = "businessAccountId", in = ParameterIn.PATH, required = true, example = DTOExamples.BUSINESS_ACCOUNT_ID) @PathVariable UUID businessAccountId) {
        Response rs = new Response();
        try {
            domainService.deleteBusinessAccount(
                    domainService.checkDomainId(domainId, UUIDCheckService.CheckMode.NOT_EMPTY_AND_DB_EXISTS),
                    businessAccountId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
