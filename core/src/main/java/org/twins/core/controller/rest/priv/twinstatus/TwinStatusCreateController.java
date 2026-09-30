package org.twins.core.controller.rest.priv.twinstatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateRqDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusListRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinstatus.TwinStatusService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.cambium.common.util.MultipartFileUtils.convert;

@Tag(description = "", name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TWIN_STATUS_CREATE)
@Slf4j
public class TwinStatusCreateController extends ApiController {
    private final TwinStatusCreateRestDTOReverseMapper twinStatusCreateRestDTOReverseMapper;
    private final TwinStatusService twinStatusService;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusCreateV1", summary = "Create new twin statuses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_status/v1")
    public ResponseEntity<?> twinStatusCreateV1(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid TwinStatusCreateRqDTOv1 request) {
        return processCreationRequest(request, mapperContext, null, null);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusCreateV2", summary = "Create new twin statuses with icons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(path = "/private/twin_status/v2", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Loggable(value = false, rqBodyThreshold = 0)
    public ResponseEntity<?> twinStatusCreateV2(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Schema(implementation = TwinStatusCreateRqDTOv1.class, requiredMode = Schema.RequiredMode.REQUIRED, description = "request json")
            @RequestPart("request") byte[] requestBytes,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Dark icon")
            @RequestPart(required = false) MultipartFile iconDark,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Light icon")
            @RequestPart(required = false) MultipartFile iconLight) throws IOException {

        var request = objectMapper.readValue(requestBytes, TwinStatusCreateRqDTOv1.class);
        log.info("Came create twin status /private/twin_status/v2 : {}", new String(requestBytes));
        return processCreationRequest(request, mapperContext, iconDark, iconLight);
    }


    protected ResponseEntity<? extends Response> processCreationRequest(TwinStatusCreateRqDTOv1 request, MapperContext mapperContext, MultipartFile iconDark, MultipartFile iconLight) {
        TwinStatusListRsDTOv1 rs = new TwinStatusListRsDTOv1();
        try {
            List<I18nEntity> namesI18n = new ArrayList<>(request.getStatuses().size());
            List<I18nEntity> descriptionsI18n = new ArrayList<>(request.getStatuses().size());
            for (TwinStatusCreateDTOv1 status : request.getStatuses()) {
                namesI18n.add(i18NSaveRestDTOReverseMapper.convert(status.getNameI18n()));
                descriptionsI18n.add(i18NSaveRestDTOReverseMapper.convert(status.getDescriptionI18n()));
            }
            List<TwinStatusEntity> twinStatusEntities = twinStatusCreateRestDTOReverseMapper.convertCollection(request.getStatuses());
            List<TwinStatusEntity> createdStatuses = twinStatusService.createStatuses(twinStatusEntities, namesI18n, descriptionsI18n, convert(iconLight), convert(iconDark));
            rs
                    .setStatuses(twinStatusRestDTOMapper.convertCollection(createdStatuses, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
