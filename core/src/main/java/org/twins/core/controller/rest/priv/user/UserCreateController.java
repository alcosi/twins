package org.twins.core.controller.rest.priv.user;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.Response;
import org.twins.core.service.UUIDCheckService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.BusinessAccountService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(description = "", name = "user")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserCreateController extends ApiController {
    private final AuthService authService;
    private final UserService userService;
    private final BusinessAccountService businessAccountService;

    @Operation(operationId = "userCreateV1", summary = "New userId registration. If BusinessAccountId header is not empty, this api will also map given userId to businessAccount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/v1", method = RequestMethod.POST)
    public ResponseEntity<?> userCreate(
            @RequestHeader("UserId") UUID userId,
            @RequestHeader("DomainId") UUID domainId,
            @RequestHeader(value = "BusinessAccountId", required = false) UUID businessAccountId,
            @RequestHeader("Channel") String channel) {
        Response rs = new Response();
        try {
            ApiUser apiUser = authService.getApiUser(
                    UUIDCheckService.CheckMode.NOT_EMPTY_AND_DB_MISSING,
                    UUIDCheckService.CheckMode.EMPTY_OR_DB_EXISTS,
                    UUIDCheckService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
            userService.addUser(apiUser.userId());
            if (businessAccountId != null)
                businessAccountService.addUser(apiUser.userId(), apiUser.businessAccountId());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
