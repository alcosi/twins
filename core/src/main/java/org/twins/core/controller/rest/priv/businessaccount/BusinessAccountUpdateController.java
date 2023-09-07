package org.twins.core.controller.rest.priv.businessaccount;

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
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.businessaccount.BusinessAccountUpdateRqDTOv1;

import org.twins.core.service.EntitySmartService;
import org.twins.core.service.businessaccount.BusinessAccountService;

import java.util.UUID;

@Tag(description = "", name = "businessAccount")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BusinessAccountUpdateController extends ApiController {
    private final BusinessAccountService businessAccountService;

    @Operation(operationId = "businessAccountUpdateV1", summary = "Update businessAccount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/business_account/{businessAccountId}/v1", method = RequestMethod.PUT)
    public ResponseEntity<?> businessAccountUpdateV1(
            @Parameter(name = "channel", in = ParameterIn.HEADER, required = true, example = DTOExamples.CHANNEL) String channel,
            @Parameter(name = "businessAccountId", in = ParameterIn.PATH, required = true, example = DTOExamples.DOMAIN_ID) @PathVariable UUID businessAccountId,
            @RequestBody BusinessAccountUpdateRqDTOv1 request) {
        Response rs = new Response();
        try {
            BusinessAccountEntity businessAccountEntity = new BusinessAccountEntity()
                    .id(businessAccountService.checkBusinessAccountId(businessAccountId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS))
                    .name(request.name());
            businessAccountService.updateBusinessAccount(businessAccountEntity);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}