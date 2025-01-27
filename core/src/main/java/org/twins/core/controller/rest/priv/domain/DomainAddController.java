package org.twins.core.controller.rest.priv.domain;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserNoDomainHeaders;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainType;
import org.twins.core.domain.apiuser.BusinessAccountResolverNotSpecified;
import org.twins.core.domain.apiuser.LocaleResolverGivenOrSystemDefault;
import org.twins.core.domain.apiuser.UserResolverAuthToken;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.DomainCreateRqDTOv1;
import org.twins.core.dto.rest.domain.DomainViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.cambium.common.util.MultipartFileUtils.convert;

@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DomainAddController extends ApiController {
    private final DomainService domainService;
    private final AuthService authService;
    private final DomainAddRestDTOReverseMapper domainAddRestDTOReverseMapper;
    private final DomainViewRestDTOMapper domainViewRestDTOMapper;
    private final UserResolverAuthToken userResolverAuthToken;

    @Deprecated
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
    @Operation(operationId = "domainAddV1", summary = "Add new domain.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was added", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DomainViewRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(path = "/private/domain/v1", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> domainAddV1(
            //There is might be problem with encodings, so better to receive byte array and convert manually
            @Schema(implementation = String.class,requiredMode = Schema.RequiredMode.REQUIRED , description = "will be used for url generation and for twins aliases", example = DTOExamples.DOMAIN_KEY)
            @RequestPart("key") byte[] keyBytes,
            @Schema(implementation = String.class, description = "domain description", example = DTOExamples.DOMAIN_DESCRIPTION)
            @RequestPart("description") byte[] descriptionBytes,
            @Schema(implementation = DomainType.class,requiredMode = Schema.RequiredMode.REQUIRED, description = "type [basic/b2b]", example = DTOExamples.DOMAIN_TYPE)
            @RequestPart("type") byte[] typeBytes,
            @Schema(implementation = String.class, description = "default locale for domain [en/de/by]", example = DTOExamples.LOCALE)
            @RequestPart("defaultLocale") byte[] defaultLocaleBytes,
            @Schema(implementation = UUID.class, description = "Resource storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
            @RequestPart("resourceStorageId") byte[] resourceStorageId,
            @Schema(implementation = UUID.class, description = "Attachment storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
            @RequestPart("attachmentStorageId") byte[] attachmentStorageId,
            @RequestPart MultipartFile iconDark,
            @RequestPart MultipartFile iconLight) {
        DomainCreateRqDTOv1 request = mapDomainRequestFromBytesToDto(keyBytes, descriptionBytes, typeBytes, defaultLocaleBytes,resourceStorageId,attachmentStorageId);
        return processCreationRequest(request, iconDark, iconLight);
    }

    public UUID attachmentStorageId;
    protected  DomainCreateRqDTOv1 mapDomainRequestFromBytesToDto(byte[] keyBytes, byte[] descriptionBytes, byte[] typeBytes, byte[] defaultLocaleBytes,byte[] resourceStorageId,byte[] attachmentStorageId) {
        DomainCreateRqDTOv1 request=new DomainCreateRqDTOv1();
        request.key= keyBytes ==null|| keyBytes.length==0?null:new String(keyBytes, StandardCharsets.UTF_8);
        request.description = descriptionBytes ==null|| descriptionBytes.length==0?null:new String(descriptionBytes, StandardCharsets.UTF_8);
        request.type= typeBytes ==null|| typeBytes.length==0?null:DomainType.valueOf(new String(typeBytes, StandardCharsets.UTF_8));
        request.defaultLocale= defaultLocaleBytes ==null|| defaultLocaleBytes.length==0?null:new String(defaultLocaleBytes, StandardCharsets.UTF_8);
        request.resourceStorageId= resourceStorageId ==null|| resourceStorageId.length==0?null:UUID.fromString(new String(resourceStorageId, StandardCharsets.UTF_8));
        request.attachmentStorageId= attachmentStorageId ==null|| attachmentStorageId.length==0?null:UUID.fromString(new String(attachmentStorageId, StandardCharsets.UTF_8));
        return request;
    }

    protected ResponseEntity<? extends Response> processCreationRequest(DomainCreateRqDTOv1 request, MultipartFile iconDark, MultipartFile iconLight) {
        DomainViewRsDTOv1 rs = new DomainViewRsDTOv1();
        try {
            authService.getApiUser()
                    .setUserResolver(userResolverAuthToken)
                    .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified())
                    .setLocaleResolver(new LocaleResolverGivenOrSystemDefault(request.defaultLocale));
            DomainEntity domainEntity = domainService.addDomain(domainAddRestDTOReverseMapper.convert(request),convert(iconLight),convert(iconDark));
            rs.setDomain(domainViewRestDTOMapper.convert(domainEntity));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
