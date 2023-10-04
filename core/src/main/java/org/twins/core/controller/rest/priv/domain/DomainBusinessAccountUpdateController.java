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
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUpdateRqDTOv1;
import org.twins.core.service.domain.DomainService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainBusinessAccountUpdateController extends ApiController {
    private final DomainService domainService;
    @Operation(operationId = "domainBusinessAccountUpdateV1", summary = "Update settings for businessAccount in domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BusinessAccount was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/domain/{domainId}/business_account/{businessAccountId}/v1", method = RequestMethod.POST)
    public ResponseEntity<?> domainBusinessAccountUpdateV1(
            @Parameter(name = "domainId", in = ParameterIn.PATH, required = true, example = DTOExamples.DOMAIN_ID) @PathVariable UUID domainId,
            @Parameter(name = "channel", in = ParameterIn.HEADER, required = true, example = DTOExamples.CHANNEL) String channel,
            @Parameter(name = "businessAccountId", in = ParameterIn.PATH, required = true, example = DTOExamples.BUSINESS_ACCOUNT_ID) @PathVariable UUID businessAccountId,
            @RequestBody DomainBusinessAccountUpdateRqDTOv1 request) {
        Response rs = new Response();
        try {
            domainService.updateDomainBusinessAccount(new DomainBusinessAccountEntity()
                    .setDomainId(domainId)
                    .setBusinessAccountId(businessAccountId)
                    .setPermissionSchemaId(request.permissionSchemaId)
                    .setTwinClassSchemaId(request.twinClassSchemaId)
                    .setTwinflowSchemaId(request.twinFlowSchemaId));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
