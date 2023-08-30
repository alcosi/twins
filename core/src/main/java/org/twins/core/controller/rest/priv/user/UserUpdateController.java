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
import org.twins.core.dto.rest.user.UserUpdateRqDTOv1;
import org.twins.core.service.UUIDCheckService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(description = "", name = "user")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserUpdateController extends ApiController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(operationId = "userUpdateV1", summary = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/v1", method = RequestMethod.PUT)
    public ResponseEntity<?> userCreate(
            @RequestHeader("UserId") UUID userId,
            @RequestHeader("DomainId") UUID domainId,
            @RequestHeader("Channel") String channel,
            @RequestBody UserUpdateRqDTOv1 request) {
        Response rs = new Response();
        try {
            ApiUser apiUser = authService.getApiUser(
                    UUIDCheckService.CheckMode.NOT_EMPTY_AND_DB_EXISTS,
                    UUIDCheckService.CheckMode.ANY,
                    UUIDCheckService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
            userService.updateUser(apiUser.userId(), request.name(), request.email(), request.avatar());
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
