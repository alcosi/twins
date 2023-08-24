package org.twins.core.controller.rest.priv.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.Response;
import org.twins.core.mappers.rest.twin.TwinListRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.BusinessAccountService;
import org.twins.core.service.user.UserService;

import java.util.List;
import java.util.UUID;

@Tag(description = "Use CRUD", name = "user")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserCRUDController extends ApiController {
    private final UserService userService;
    private final BusinessAccountService businessAccountService;

    @Operation(operationId = "userCreateV", summary = "")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added"),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/v1", method = RequestMethod.POST)
    public ResponseEntity<?> userCreate(
            @RequestHeader("UserId") UUID userId,
            @RequestHeader("DomainId") UUID domainId,
            @RequestHeader(value = "BusinessAccountId", required = false) UUID businessAccountId,
            @RequestHeader("Channel") String channel) {
        Response rs = new Response();
        try {
            userService.addUser(userId);
            if (businessAccountId != null)
                businessAccountService.addUser(userId, businessAccountId);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
