package org.twins.core.controller.rest.priv.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.ParametersApiUserNoDomainHeaders;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.BusinessAccountResolverNotSpecified;
import org.twins.core.domain.apiuser.DomainResolverNotSpecified;
import org.twins.core.domain.apiuser.LocaleResolverGivenOrSystemDefault;
import org.twins.core.domain.apiuser.UserResolverAuthToken;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.DomainCreateRqDTOv1;
import org.twins.core.dto.rest.domain.DomainViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.io.IOException;

import static org.cambium.common.util.MultipartFileUtils.convert;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class DomainAddController extends ApiController {
    private final DomainService domainService;
    private final AuthService authService;
    private final DomainAddRestDTOReverseMapper domainAddRestDTOReverseMapper;
    private final DomainViewRestDTOMapper domainViewRestDTOMapper;
    private final UserResolverAuthToken userResolverAuthToken;
    private final ObjectMapper objectMapper;

    @ParametersApiUserNoDomainHeaders
    @Operation(operationId = "domainAddV1", summary = "Add new domain.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was added", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DomainViewRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/domain/v1", consumes = "application/json")
    public ResponseEntity<?> domainAddV1(
            @RequestBody DomainCreateRqDTOv1 request) {
        return processCreationRequest(request, null, null);
    }

    @ParametersApiUserNoDomainHeaders
    @Operation(operationId = "domainAddV2", summary = "Add new domain with icons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was added", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DomainViewRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(path = "/private/domain/v2", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Loggable(value = false, rqBodyThreshold = 0)
    public ResponseEntity<?> domainAddV2(
            @Schema(implementation = DomainCreateRqDTOv1.class, requiredMode = Schema.RequiredMode.REQUIRED, description = "request json")
            @RequestPart("request") byte[] requestBytes,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Dark icon")
            @RequestPart MultipartFile iconDark,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Light icon")
            @RequestPart MultipartFile iconLight) throws IOException {
        var request = objectMapper.readValue(requestBytes, DomainCreateRqDTOv1.class);
        log.info("Came add domain /private/domain/v1 : {}", new String(requestBytes));
        return processCreationRequest(request, iconDark, iconLight);
    }


    protected ResponseEntity<? extends Response> processCreationRequest(DomainCreateRqDTOv1 request, MultipartFile iconDark, MultipartFile iconLight) {
        DomainViewRsDTOv1 rs = new DomainViewRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            apiUser
                    .setUserResolver(userResolverAuthToken)
                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified())
                    .setDomainResolver(new DomainResolverNotSpecified())
                    .setLocaleResolver(new LocaleResolverGivenOrSystemDefault(request.defaultLocale))
                    .setCheckMembershipMode(false);
            DomainEntity domainEntity = domainService.addDomain(domainAddRestDTOReverseMapper.convert(request), convert(iconLight), convert(iconDark));
            rs.setDomain(domainViewRestDTOMapper.convert(domainEntity));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
