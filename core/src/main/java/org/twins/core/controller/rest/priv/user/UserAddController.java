package org.twins.core.controller.rest.priv.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParameterChannelHeader;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dto.rest.user.UserAddRqDTOv2;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.BusinessAccountResolverGivenId;
import org.twins.core.domain.apiuser.DomainResolverGivenId;
import org.twins.core.domain.apiuser.LocaleResolverGivenOrSystemDefault;
import org.twins.core.domain.apiuser.UserResolverGivenId;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.user.UserAddRqDTOv1;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountUserService;
import org.twins.core.service.domain.DomainBusinessAccountService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.user.UserService;

import java.util.UUID;

@Tag(name = ApiTag.USER)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserAddController extends ApiController {
    private final BusinessAccountUserService businessAccountUserService;
    private final DomainService domainService;
    private final DomainUserService domainUserService;
    private final DomainBusinessAccountService domainBusinessAccountService;
    private final UserService userService;
    private final AuthService authService;

    @Value("${api.unsecured.enable}")
    private boolean apiUnsecuredEnabled;

    @Deprecated
    @ParameterChannelHeader
    @Operation(operationId = "userAddV1", summary = "Smart endpoint for adding new user. It will also" +
            " add user to domain and businessAccount if specified. If given businessAccount is not registered in domain, it will register it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user/v1")
    public ResponseEntity<?> userAddV1(
            @RequestBody UserAddRqDTOv1 request) {
        Response rs = new Response();
        try {
            if (!apiUnsecuredEnabled)
                throw new ServiceException(ErrorCodeCommon.FORBIDDEN);
            // for deprecated fields
            UUID businessAccountId = request.getUser().getBusinessAccountId() != null ? request.getUser().getBusinessAccountId() : request.getBusinessAccountId();
            UUID domainId = request.getUser().getDomainId() != null ? request.getUser().getDomainId() : request.getDomainId();
            String locale = request.getUser().getLocale() != null ? request.getUser().getLocale() : request.getLocale();
            domainService.checkId(domainId, EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS);
            ApiUser apiUser = authService.getApiUser();
            apiUser
                    .setBusinessAccountResolver(new BusinessAccountResolverGivenId(businessAccountId))
                    .setUserResolver(new UserResolverGivenId(request.user.id))
                    .setDomainResolver(new DomainResolverGivenId(domainId))
                    .setLocaleResolver(new LocaleResolverGivenOrSystemDefault(locale))
                    .setCheckMembershipMode(false);
            UserEntity userEntity = userService.addUser(new UserEntity()
                            .setId(request.user.id)
                            .setName(request.user.fullName)
                            .setEmail(request.user.email)
                            .setAvatar(request.user.avatar)
                            .setUserStatusId(UserStatus.ACTIVE),
                    EntitySmartService.SaveMode.ifPresentThrowsElseCreate
            );
            // if domain is empty and BA is empty - all done
            if (businessAccountId == null && domainId == null)
                return new ResponseEntity<>(rs, HttpStatus.OK);
            if (businessAccountId != null) {
                businessAccountUserService.addUserSmart(businessAccountId, request.user.id, EntitySmartService.SaveMode.ifNotPresentCreate, EntitySmartService.SaveMode.none, true);
            }
            if (domainId != null) {
                domainUserService.addUser(userEntity, true);
            }
            if (domainId != null && businessAccountId != null && apiUser.getDomain().getDomainType() == DomainType.b2b) {
                domainBusinessAccountService.addBusinessAccountSmart(businessAccountId, null, null, EntitySmartService.SaveMode.none, true);
            }
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


    @ProtectedBy({Permissions.USER_MANAGE, Permissions.USER_CREATE})
    @ParametersApiUserHeaders
    @Operation(operationId = "userAddV2", summary = "User add protected API for clients with M2M tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User was added", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/user/v2")
    public ResponseEntity<?> userAddV2(
            @RequestBody UserAddRqDTOv2 request) {
        Response rs = new Response();
        try {
            ApiUser apiUser = authService.getApiUser();

            UUID businessAccountId = request.getUser().getBusinessAccountId();
            UUID domainId = request.getUser().getDomainId();

            if (domainId != null) {
                domainService.checkId(domainId, EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS);
            }

            apiUser
                    .setBusinessAccountResolver(new BusinessAccountResolverGivenId(businessAccountId))
                    .setUserResolver(new UserResolverGivenId(request.getUser().getId()))
                    .setDomainResolver(new DomainResolverGivenId(domainId))
                    .setLocaleResolver(new LocaleResolverGivenOrSystemDefault(request.getUser().getLocale()))
                    .setCheckMembershipMode(false);

            UserEntity userEntity = userService.addUser(new UserEntity()
                            .setId(request.getUser().getId())
                            .setName(request.getUser().getFullName())
                            .setEmail(request.getUser().getEmail())
                            .setAvatar(request.getUser().getAvatar())
                            .setUserStatusId(UserStatus.ACTIVE),
                    EntitySmartService.SaveMode.ifPresentThrowsElseCreate
            );

            if (businessAccountId == null && domainId == null)
                return new ResponseEntity<>(rs, HttpStatus.OK);

            if (businessAccountId != null) {
                businessAccountUserService.addUserSmart(businessAccountId, request.getUser().getId(), EntitySmartService.SaveMode.ifNotPresentCreate, EntitySmartService.SaveMode.none, true);
            }

            if (domainId != null) {
                domainUserService.addUser(userEntity, true);
            }

            if (domainId != null && businessAccountId != null && apiUser.getDomain() != null && apiUser.getDomain().getDomainType() == DomainType.b2b) {
                domainBusinessAccountService.addBusinessAccountSmart(businessAccountId, null, null, EntitySmartService.SaveMode.none, true);
            }

        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
