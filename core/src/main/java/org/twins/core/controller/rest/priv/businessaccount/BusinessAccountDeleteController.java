package org.twins.core.controller.rest.priv.businessaccount;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.BUSINESS_ACCOUNT)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class BusinessAccountDeleteController extends ApiController {
    final BusinessAccountService businessAccountService;
    final AuthService authService;

    @ParameterChannelHeader
    @Operation(operationId = "businessAccountDeleteV1", summary = "Delete businessAccount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/business_account/{businessAccountId}/v1", method = RequestMethod.DELETE)
    public ResponseEntity<?> businessAccountDeleteV1(
            @Parameter(example = DTOExamples.BUSINESS_ACCOUNT_ID) @PathVariable UUID businessAccountId) {
        Response rs = new Response();
        try {
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
//        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
