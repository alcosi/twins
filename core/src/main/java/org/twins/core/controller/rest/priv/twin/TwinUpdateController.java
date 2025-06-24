package org.twins.core.controller.rest.priv.twin;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.TwinRsDTOv2;
import org.twins.core.dto.rest.twin.TwinUpdateRqDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twin.TwinUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final ObjectMapper objectMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinUpdateV1", summary = "Update twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin/{twinId}/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinUpdateV1(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
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
    @ParametersApiUserHeaders
    @Operation(summary = "twinUpdateV1", description = "Updates a twin using a multipart form. The twin data should be a JSON string in the 'request' form field.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TwinRsDTOv2.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PutMapping(value = "/private/twin/{twinId}/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinUpdateFromMultipart(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId,
            @Schema(hidden = true) MultipartHttpServletRequest request, @Schema(implementation = TwinUpdateRqDTOv1.class) @RequestPart("request") byte[] requestBytes) {
        // Spring can automatically convert the JSON part to your DTO
        // if a proper HttpMessageConverter (like MappingJackson2HttpMessageConverter) is configured.
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
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            // update twin
            TwinEntity dbTwinEntity = twinService.findEntity(twinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            twinAttachmentCUDRestDTOReverseMapper.preProcessAttachments(request.attachments, filesMap);

            TwinUpdate twinUpdate = twinUpdateRestDTOReverseMapper.convert(Pair.of(request.setTwinId(twinId), dbTwinEntity))
                    .setCheckEditPermission(true);
            twinService.updateTwin(twinUpdate);

            // get twin by id and set result based on mapper context

            rs
                    .setTwin(twinRestDTOMapperV2.convert(twinService.findEntitySafe(twinId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    protected <T> T mapRequest(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (Throwable t) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, t.getMessage());
        }
    }
}
