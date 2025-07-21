package org.twins.core.controller.rest.priv.twin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.link.TwinLinkAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRqRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinCreateRsRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_CREATE})
public class TwinCreateController extends ApiController {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinFieldValueRestDTOReverseMapper twinFieldValueRestDTOReverseMapper;
    private final UserService userService;
    private final TwinCreateRsRestDTOMapper twinCreateRsRestDTOMapper;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;
    private final TwinLinkAddRestDTOReverseMapper twinLinkAddRestDTOReverseMapper;
    private final TwinCreateRqRestDTOReverseMapper twinCreateRqRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinCreateV1", summary = "Create new twin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = TwinCreateRsDTOv1.class)),}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/v1", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCreateV1(
            @RequestBody TwinCreateRqDTOv1 request) {
        TwinCreateRsDTOv1 rs = new TwinCreateRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            List<TwinFieldValueDTO> fields = new ArrayList<>();
            if (request.getFields() != null)
                for (Map.Entry<String, TwinFieldValueDTO> entry : request.getFields().entrySet())
                    fields.add(entry.getValue()
                            .fieldKey(entry.getKey())
                            .twinClassId(request.getClassId()));
            TwinCreate twinCreate = new TwinCreate();
            twinCreate
                    .setFields(twinFieldValueRestDTOReverseMapper.convertCollection(fields))
                    .setTwinEntity(new TwinEntity()
                            .setTwinClassId(request.getClassId())
                            .setName(request.getName())
                            .setCreatedByUserId(apiUser.getUser().getId())
                            .setHeadTwinId(request.getHeadTwinId())
                            .setAssignerUserId(userService.checkId(request.getAssignerUserId(), EntitySmartService.CheckMode.EMPTY_OR_DB_EXISTS))
                            .setDescription(request.getDescription()));
            twinCreate
                    .setAttachmentEntityList(attachmentCreateRestDTOReverseMapper.convertCollection(request.getAttachments()))
                    .setLinksEntityList(twinLinkAddRestDTOReverseMapper.convertCollection(request.getLinks()))
                    .setCheckCreatePermission(true);
            rs = twinCreateRsRestDTOMapper
                    .convert(twinService
                            .createTwin(twinCreate));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }


    /**
     * Endpoint for creating a twin from a JSON request body.
     */
    @ParametersApiUserHeaders
    @Operation(summary = "twinCreateV2", description = "Creates a new twin using a standard JSON payload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TwinCreateRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/twin/v2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCreateFromJson(@RequestBody TwinCreateRqDTOv2 request) {
        return createTwinV2(request, Map.of());
    }

    /**
     * Endpoint for creating a twin from a multipart/form-data request.
     * The DTO is expected as a JSON string in the 'request' part.
     * You could also add other parts, e.g., @RequestPart("file") MultipartFile file.
     */
    @SneakyThrows
    @Operation(summary = "twinCreateV2", description = "Creates a new twin using a multipart form. The twin data should be a JSON string in the 'request' form field.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TwinCreateRsDTOv1.class))
            }),
            @ApiResponse(responseCode = "401", description = "Access is denied")
    })
    @PostMapping(value = "/private/twin/v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinCreateFromMultipart(
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = TwinCreateRqDTOv2.class) @RequestPart("request") byte[] requestBytes) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return createTwinV2(mapRequest(requestBytes, TwinCreateRqDTOv2.class), filesMap);
    }

    protected ResponseEntity<Response> createTwinV2(TwinCreateRqDTOv2 request, Map<String, MultipartFile> filesMap) {
        TwinCreateRsDTOv1 rs = new TwinCreateRsDTOv1();
        try {
            attachmentCreateRestDTOReverseMapper.preProcessAttachments(request.attachments, filesMap);
            TwinCreate twinCreate = twinCreateRqRestDTOReverseMapper.convert(request).setCheckCreatePermission(true);
            rs = twinCreateRsRestDTOMapper
                    .convert(twinService
                            .createTwin(twinCreate));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinBatchCreateV1", summary = "Create batch twins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import was completed successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/batch/v1", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinBatchCreateV1(
            @RequestBody TwinBatchCreateRqDTOv1 request) {
        return processBatch(request, Map.of());
    }

    @SneakyThrows
    @ParametersApiUserHeaders
    @Operation(operationId = "twinBatchCreateV1", summary = "Create batch twins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import was completed successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin/batch/v1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinBatchCreateV1Multipart(
            @Schema(hidden = true) MultipartHttpServletRequest request,
            @Schema(implementation = TwinBatchCreateRqDTOv1.class) @RequestPart("request") byte[] requestBytes
    ) {
        Map<String, MultipartFile> filesMap = new HashMap<>();
        request.getFileNames().forEachRemaining(fileName -> {
            List<MultipartFile> files = request.getFiles(fileName);
            files.forEach(file -> {
                filesMap.put(fileName, file);
            });
        });
        return processBatch(mapRequest(requestBytes, TwinBatchCreateRqDTOv1.class), filesMap);
    }

    protected ResponseEntity<Response> processBatch(TwinBatchCreateRqDTOv1 request, Map<String, MultipartFile> filesMap) {
        Response rs = new Response();
        try {
            request.getTwins().forEach(twinCreateRqDTOv1 -> {
                attachmentCreateRestDTOReverseMapper.preProcessAttachments(twinCreateRqDTOv1.attachments, filesMap);
            });
            List<TwinCreate> twinCreates = twinCreateRqRestDTOReverseMapper.convertCollection(request.getTwins());
            for (TwinCreate twinCreate : twinCreates) {
                twinCreate.setCheckCreatePermission(true);
            }
            twinService.createTwinsAsyncBatch(twinCreates);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
