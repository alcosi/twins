package org.twins.core.controller.rest.priv.user;

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
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.user.UserUpdateRqDTOv1;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.USER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserUpdateController extends ApiController {
    private final UserService userService;

    @ParameterChannelHeader
    @Operation(operationId = "userUpdateV1", summary = "Update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was updated", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/user/{userId}/v1")
    public ResponseEntity<?> userCreate(
            @Parameter(example = DTOExamples.USER_ID) @PathVariable UUID userId,
            @RequestBody UserUpdateRqDTOv1 request) {
        Response rs = new Response();
        try {
            UserEntity userEntity = new UserEntity()
                    .setId(userId)
                    .setName(request.fullName())
                    .setEmail(request.email())
                    .setAvatar(request.avatar())
                    .setUserStatusId(UserStatus.ACTIVE);
            userService.updateUser(userEntity);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
