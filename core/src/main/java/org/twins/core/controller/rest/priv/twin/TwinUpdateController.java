package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.exception.TwinBatchFieldValidationException;
import org.cambium.common.exception.TwinFieldValidationException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.util.*;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_UPDATE})
public class TwinUpdateController extends ApiController {
    private final TwinService twinService;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final TwinUpdateRestDTOReverseMapper twinUpdateRestDTOReverseMapper;
    private final AttachmentCUDRestDTOReverseMapper twinAttachmentCUDRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinUpdateV1", summary = "Update twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinSaveRsV1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/{twinId}/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinUpdateV1(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSaveRsV1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @RequestBody TwinUpdateRqDTOv1 request) {
        return updateTwin(mapperContext, twinId, request, new HashMap<>());
    }


    /**
     * Endpoint for updating a twin from a multipart/form-data request.
     * The DTO is expected as a JSON string in the 'request' part.
     * You could also add other parts, e.g., @RequestPart("file") MultipartFile file.
     */
    @SneakyThrows
    @Operation(summary = "twinUpdateV1", description = "Updates a twin using a multipart form. The twin data should be a JSON string in the 'request' form field.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TwinSaveRsV1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PutMapping(value = "/private/twin/{twinId}/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinUpdateFromMultipart(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinSaveRsV1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Schema(hidden = true) MultipartHttpServletRequest request, @Schema(implementation = TwinUpdateRqDTOv1.class) @RequestPart("request") byte[] requestBytes) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return updateTwin(mapperContext, twinId, mapRequest(requestBytes, TwinUpdateRqDTOv1.class), filesMap);
    }


    protected ResponseEntity<? extends Response> updateTwin(MapperContext mapperContext, UUID twinId, TwinUpdateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        TwinSaveRsV1 rs = new TwinSaveRsV1();
        try {
            if (request.getTwinId() != null && !twinId.equals(request.getTwinId())) {
                throw new ServiceException(ErrorCodeTwins.UUID_MISMATCH, " twin id in header and request body are different");
            }
            // update twin
            TwinEntity dbTwinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            twinAttachmentCUDRestDTOReverseMapper.preProcessAttachments(request.attachments, filesMap);

            TwinUpdate twinUpdate = twinUpdateRestDTOReverseMapper.convert(Pair.of(request.setTwinId(twinId), dbTwinEntity));
            twinUpdate
                    .setCheckEditPermission(true)
                    .setLauncher(TwinOperation.Launcher.direct);
            twinService.updateTwin(twinUpdate);

            // get twin by id and set result based on mapper context

            rs
                    .setTwin(twinRestDTOMapperV2.convert(twinService.findEntitySafe(twinId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (TwinFieldValidationException ve) {
            return createErrorRs(ve, rs, null);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


    @ParametersApiUserHeaders
    @Operation(operationId = "twinUpdateV2", summary = "Update twin batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/batch/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinUpdateBatchV1(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinBatchSaveRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinBatchUpdateRqDTOv1 request) {
        return updateTwinBatch(mapperContext, request, new HashMap<>());
    }

    @SneakyThrows
    @Operation(operationId = "twinUpdateV2", summary = "Update twin batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/batch/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinUpdateBatchV1Multipart(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinBatchSaveRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = TwinBatchUpdateRqDTOv1.class) @RequestPart("request") byte[] requestBytes) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return updateTwinBatch(mapperContext, mapRequest(requestBytes, TwinBatchUpdateRqDTOv1.class), filesMap);
    }

    protected ResponseEntity<? extends Response> updateTwinBatch(MapperContext mapperContext, TwinBatchUpdateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        TwinBatchSaveRsDTOv1 rs = new TwinBatchSaveRsDTOv1();
        try {
            List<UUID> twinIds = new ArrayList<>();
            for (TwinUpdateDTOv1 twinUpdateRqDTOv1 : request.getTwins()) {
                twinIds.add(twinUpdateRqDTOv1.getTwinId());
                twinAttachmentCUDRestDTOReverseMapper.preProcessAttachments(twinUpdateRqDTOv1.attachments, filesMap);
            }

            Kit<TwinEntity, UUID> dbTwinEntities = twinService.findEntities(twinIds, EntitySmartService.ListFindMode.ifMissedThrows, EntitySmartService.ReadPermissionCheckMode.none, EntitySmartService.EntityValidateMode.none);

            List<TwinUpdate> twinUpdates = new ArrayList<>();
            for (TwinUpdateRqDTOv1 twinUpdateRqDTOv1 : request.getTwins()) {
                TwinEntity dbTwinEntity = dbTwinEntities.get(twinUpdateRqDTOv1.getTwinId());

                TwinUpdate twinUpdate = twinUpdateRestDTOReverseMapper.convert(Pair.of(twinUpdateRqDTOv1, dbTwinEntity));
                twinUpdate
                        .setCheckEditPermission(true)
                        .setLauncher(TwinOperation.Launcher.direct);

                twinUpdates.add(twinUpdate);
            }

            List<TwinEntity> twinEntities = twinService.updateTwin(twinUpdates, true);

            rs
                    .setTwinList(twinRestDTOMapperV2.convertCollection(twinEntities, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));

        } catch (TwinBatchFieldValidationException ve) {
            return createErrorRs(ve, rs, null);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
