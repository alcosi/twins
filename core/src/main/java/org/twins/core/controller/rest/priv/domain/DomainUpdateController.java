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
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.domain.DomainUpdateRqDTOv1;
import org.twins.core.dto.rest.domain.DomainViewRsDTOv1;
import org.twins.core.mappers.rest.domain.DomainUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.domain.DomainViewRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.permission.Permissions;

import java.io.IOException;

import static org.cambium.common.util.MultipartFileUtils.convert;


@Tag(description = "", name = ApiTag.DOMAIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.DOMAIN_MANAGE, Permissions.DOMAIN_UPDATE})
public class DomainUpdateController extends ApiController {
    private final DomainUpdateRestDTOReverseMapper domainUpdateRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final DomainViewRestDTOMapper domainViewRestDTOMapper;
    private final DomainService domainService;

    @ParametersApiUserHeaders
    @Operation(operationId = "domainUpdateV1", summary = "Domain update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "domain was updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = DomainViewRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/domain/v1")
    public ResponseEntity<?> domainUpdateV1(
            @MapperContextBinding(roots = DomainViewRestDTOMapper.class, response = DomainViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody DomainUpdateRqDTOv1 request) {
        return processUpdate(mapperContext, request, null, null);
    }


    @ParametersApiUserHeaders
    @Operation(operationId = "domainUpdateV2", summary = "Update  domain with icons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Domain was updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DomainViewRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(path = "/private/domain/v2", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Loggable(value = false, rqBodyThreshold = 0)
    public ResponseEntity<?> domainUpdateV2(
            @MapperContextBinding(roots = DomainViewRestDTOMapper.class, response = DomainViewRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Schema(implementation = DomainUpdateRqDTOv1.class, requiredMode = Schema.RequiredMode.REQUIRED, description = "request json")
            @RequestPart("request") byte[] requestBytes,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Dark icon")
            @RequestPart MultipartFile iconDark,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Light icon")
            @RequestPart MultipartFile iconLight) throws IOException {
        var request = objectMapper.readValue(requestBytes, DomainUpdateRqDTOv1.class);
        return processUpdate(mapperContext, request, iconDark, iconLight);
    }

    protected ResponseEntity<? extends Response> processUpdate(MapperContext mapperContext, DomainUpdateRqDTOv1 request, MultipartFile iconDark, MultipartFile iconLight) {
        DomainViewRsDTOv1 rs = new DomainViewRsDTOv1();
        try {
            DomainEntity domain = domainUpdateRestDTOReverseMapper.convert(request.getDomain());
            domain = domainService.updateDomain(domain, convert(iconLight), convert(iconDark));
            rs
                    .setDomain(domainViewRestDTOMapper.convert(domain, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
