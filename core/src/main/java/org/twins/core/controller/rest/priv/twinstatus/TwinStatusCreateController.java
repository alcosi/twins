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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


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
        return processCreationRequest(request, mapperContext, Collections.emptyMap());
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
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = TwinStatusCreateRqDTOv1.class, requiredMode = Schema.RequiredMode.REQUIRED, description = "request json")
            @RequestPart("request") byte[] requestBytes) {

        Map<String, MultipartFile> filesMap = MultipartFileUtils.collectFiles(request);
        TwinStatusCreateRqDTOv1 rq = mapRequest(requestBytes, TwinStatusCreateRqDTOv1.class);
        log.info("Came create twin status /private/twin_status/v2 : {} bytes, {} statuses",
                requestBytes.length, rq.getStatuses() == null ? -1 : rq.getStatuses().size());
        return processCreationRequest(rq, mapperContext, filesMap);
    }


    protected ResponseEntity<? extends Response> processCreationRequest(TwinStatusCreateRqDTOv1 request, MapperContext mapperContext, Map<String, MultipartFile> filesMap) {
        TwinStatusListRsDTOv1 rs = new TwinStatusListRsDTOv1();
        try {
            List<I18nEntity> namesI18n = new ArrayList<>(request.getStatuses().size());
            List<I18nEntity> descriptionsI18n = new ArrayList<>(request.getStatuses().size());
            List<FileData> lightIcons = new ArrayList<>(request.getStatuses().size());
            List<FileData> darkIcons = new ArrayList<>(request.getStatuses().size());
            for (TwinStatusCreateDTOv1 status : request.getStatuses()) {
                namesI18n.add(i18NSaveRestDTOReverseMapper.convert(status.getNameI18n()));
                descriptionsI18n.add(i18NSaveRestDTOReverseMapper.convert(status.getDescriptionI18n()));
                lightIcons.add(MultipartFileUtils.resolveMultipartFile(status.getIconLightLink(), filesMap));
                darkIcons.add(MultipartFileUtils.resolveMultipartFile(status.getIconDarkLink(), filesMap));
            }
            List<TwinStatusEntity> twinStatusEntities = twinStatusCreateRestDTOReverseMapper.convertCollection(request.getStatuses());
            List<TwinStatusEntity> createdStatuses = twinStatusService.createStatuses(twinStatusEntities, namesI18n, descriptionsI18n, lightIcons, darkIcons);
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
