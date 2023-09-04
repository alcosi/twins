package org.twins.core.controller.rest.priv.user;

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
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.user.UserUpdateRqDTOv1;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(description = "", name = "user")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserUpdateController extends ApiController {
    private final UserService userService;

    @Operation(operationId = "userUpdateV1", summary = "Update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/{userId}/v1", method = RequestMethod.PUT)
    public ResponseEntity<?> userCreate(
            @Parameter(name = "channel", in = ParameterIn.HEADER, required = true, example = DTOExamples.CHANNEL) String channel,
            @Parameter(name = "userId", in = ParameterIn.PATH, required = true, example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @RequestBody UserUpdateRqDTOv1 request) {
        Response rs = new Response();
        try {
            UserEntity userEntity = new UserEntity()
                    .id(userId)
                    .name(request.name())
                    .email(request.email())
                    .avatar(request.avatar());
            userService.updateUser(userEntity);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
