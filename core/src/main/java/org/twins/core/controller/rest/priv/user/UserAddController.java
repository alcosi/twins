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
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.user.UserAddRqDTOv1;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.user.UserService;

@Tag(name = ApiTag.USER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserAddController extends ApiController {
    private final BusinessAccountService businessAccountService;
    private final DomainService domainService;
    private final UserService userService;

    @ParameterChannelHeader
    @Operation(operationId = "userAddV1", summary = "Smart endpoint for adding new user. It will also" +
            " add user to domain and businessAccount if specified. If given businessAccount is not registered in domain, it will register it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/user/v1", method = RequestMethod.POST)
    public ResponseEntity<?> userAddV1(
            @RequestBody UserAddRqDTOv1 request) {
        Response rs = new Response();
        try {
            domainService.checkDomainId(request.domainId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
            userService.addUser(new UserEntity()
                    .setId(request.user.id)
                    .setName(request.user.name)
                    .setEmail(request.user.email)
                    .setAvatar(request.user.avatar),
                    EntitySmartService.SaveMode.ifPresentThrowsElseCreate
            );
            if (request.businessAccountId != null) {
                businessAccountService.addUser(request.businessAccountId, request.user.id, EntitySmartService.SaveMode.ifNotPresentCreate);
            }
            if (request.domainId != null) {
                domainService.addUser(request.domainId, request.user.id, EntitySmartService.SaveMode.none);
            }
            if (request.domainId != null && request.businessAccountId != null) {
                domainService.addBusinessAccount(request.domainId, request.businessAccountId, true, EntitySmartService.SaveMode.none);
            }

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
