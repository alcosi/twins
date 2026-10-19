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
import org.cambium.common.file.FileData;
import org.cambium.common.util.MultipartFileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.Loggable;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinstatus.TwinStatusListRsDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRqDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinstatus.TwinStatusService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(description = "", name = ApiTag.TWIN_STATUS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy(Permissions.TWIN_STATUS_UPDATE)
public class TwinStatusUpdateController extends ApiController {
    private final TwinStatusService twinStatusService;
    private final TwinStatusUpdateRestDTOReverseMapper twinStatusUpdateRestDTOReverseMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusUpdateV1", summary = "Update twin statuses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_status/v1")
    public ResponseEntity<?> twinStatusUpdateV1(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody @Valid TwinStatusUpdateRqDTOv1 request) {
        return processUpdate(mapperContext, request, Collections.emptyMap());
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinStatusUpdateV2", summary = "Update twin statuses with icons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin status data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinStatusListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(path = "/private/twin_status/v2", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Loggable(value = false, rqBodyThreshold = 0)
    public ResponseEntity<?> twinStatusUpdateV2(
            @MapperContextBinding(roots = TwinStatusRestDTOMapper.class, response = TwinStatusListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = TwinStatusUpdateRqDTOv1.class, requiredMode = Schema.RequiredMode.REQUIRED, description = "request json")
            @RequestPart("request") byte[] requestBytes) {
        Map<String, MultipartFile> filesMap = MultipartFileUtils.collectFiles(request);
        TwinStatusUpdateRqDTOv1 rq = mapRequest(requestBytes, TwinStatusUpdateRqDTOv1.class);
        log.info("Came update twin status /private/twin_status/v2 : {} bytes, {} statuses",
                requestBytes.length, rq.getStatuses() == null ? -1 : rq.getStatuses().size());
        return processUpdate(mapperContext, rq, filesMap);
    }

    protected ResponseEntity<? extends Response> processUpdate(MapperContext mapperContext, TwinStatusUpdateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        TwinStatusListRsDTOv1 rs = new TwinStatusListRsDTOv1();
        try {
            List<I18nEntity> namesI18n = new ArrayList<>(request.getStatuses().size());
            List<I18nEntity> descriptionsI18n = new ArrayList<>(request.getStatuses().size());
            List<FileData> lightIcons = new ArrayList<>(request.getStatuses().size());
            List<FileData> darkIcons = new ArrayList<>(request.getStatuses().size());
            for (TwinStatusUpdateDTOv1 status : request.getStatuses()) {
                namesI18n.add(i18NSaveRestDTOReverseMapper.convert(status.getNameI18n()));
                descriptionsI18n.add(i18NSaveRestDTOReverseMapper.convert(status.getDescriptionI18n()));
                lightIcons.add(MultipartFileUtils.resolveMultipartFile(status.getIconLightLink(), filesMap));
                darkIcons.add(MultipartFileUtils.resolveMultipartFile(status.getIconDarkLink(), filesMap));
            }
            List<TwinStatusEntity> updatedStatuses = twinStatusService.updateStatuses(
                    twinStatusUpdateRestDTOReverseMapper.convertCollection(request.getStatuses()), namesI18n, descriptionsI18n, lightIcons, darkIcons);
            rs
                    .setStatuses(twinStatusRestDTOMapper.convertCollection(updatedStatuses, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
