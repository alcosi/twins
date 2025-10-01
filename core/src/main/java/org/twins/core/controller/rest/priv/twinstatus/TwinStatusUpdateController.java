package org.twins.core.controller.rest.priv.twinstatus;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinStatusService;

import java.io.IOException;
import java.util.UUID;

import static org.cambium.common.util.MultipartFileUtils.convert;

@Tag(description = "", name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_STATUS_MANAGE, Permissions.TWIN_STATUS_UPDATE})
public class TwinStatusUpdateController extends ApiController {
    private final TwinStatusService twinStatusService;
    private final TwinStatusRestDTOReverseMapper twinStatusRestDTOReverseMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusUpdateV1", summary = "Update twin status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_status/{twinStatusId}/v1")
    public ResponseEntity<?> twinStatusUpdateV1(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_STATUS_ID) @PathVariable UUID twinStatusId,
            @RequestBody TwinStatusUpdateRqDTOv1 request) {
        return processUpdate(mapperContext, twinStatusId, request, null, null);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusUpdateV2", summary = "Update twin status with icons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(path = "/private/twin_status/{twinStatusId}/v2", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> twinStatusUpdateV2(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_STATUS_ID) @PathVariable UUID twinStatusId,
            @Schema(implementation = TwinStatusUpdateRqDTOv1.class, requiredMode = Schema.RequiredMode.REQUIRED, description = "request json")
            @RequestPart("request") byte[] requestBytes,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Dark icon")
            @RequestPart(required = false) MultipartFile iconDark,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Light icon")
            @RequestPart(required = false) MultipartFile iconLight) throws IOException {
        var request = objectMapper.readValue(requestBytes, TwinStatusUpdateRqDTOv1.class);
        return processUpdate(mapperContext, twinStatusId, request, iconDark, iconLight);
    }

    protected ResponseEntity<? extends Response> processUpdate(MapperContext mapperContext, UUID twinStatusId, TwinStatusUpdateRqDTOv1 request, MultipartFile iconDark, MultipartFile iconLight) {
        TwinStatusUpdateRsDTOv1 rs = new TwinStatusUpdateRsDTOv1();
        try {
            I18nEntity nameI18n = i18NSaveRestDTOReverseMapper.convert(request.getNameI18n());
            I18nEntity descriptionI18n = i18NSaveRestDTOReverseMapper.convert(request.getDescriptionI18n());
            TwinStatusEntity twinStatusEntity = twinStatusService
                    .updateStatus(twinStatusRestDTOReverseMapper.convert(request.setId(twinStatusId)), nameI18n, descriptionI18n, convert(iconLight), convert(iconDark));
            rs
                    .setTwinStatus(twinStatusRestDTOMapper.convert(twinStatusEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
