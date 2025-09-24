package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinclass.TwinClassRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassUpdateRqDTOv2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassUpdateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassUpdateRestDTOReverseMapperV2;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.List;
import java.util.UUID;

import static org.cambium.common.util.MultipartFileUtils.convert;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_UPDATE})
public class TwinClassUpdateController extends ApiController {
    private final TwinClassService twinClassService;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinClassUpdateRestDTOReverseMapper twinClassUpdateRestDTOReverseMapper;
    private final TwinClassUpdateRestDTOReverseMapperV2 twinClassUpdateRestDTOReverseMapperV2;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassUpdateV1", summary = "Update twin class by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_class/{twinClassId}/v1")
    public ResponseEntity<?> twinClassUpdateV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestBody TwinClassUpdateRqDTOv1 request) {
        TwinClassRsDTOv1 rs = new TwinClassRsDTOv1();
        try {
            TwinClassEntity twinClassEntity = twinClassService.updateTwinClasses(twinClassUpdateRestDTOReverseMapper.convert(request.setTwinClassId(twinClassId)), null, null);
            rs
                    .setTwinClass(twinClassRestDTOMapper.convert(twinClassEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassUpdateV2", summary = "Update twin classes batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin classes updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_class/v2")
    public ResponseEntity<?> twinClassUpdateV2(
            @RequestBody TwinClassUpdateRqDTOv2 request) {
        return processUpdateBatch(request, null, null);
    }

    @SneakyThrows
    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassUpdateV2Multipart", summary = "Update twin classes batch with icons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin classes updated successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twin_class/v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinClassUpdateV2Multipart(
            @Schema(implementation = TwinClassUpdateRqDTOv2.class, requiredMode = Schema.RequiredMode.REQUIRED)
            @RequestPart("request") byte[] requestBytes,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Dark icon for all twin classes")
            @RequestPart(required = false) MultipartFile iconDark,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Light icon for all twin classes")
            @RequestPart(required = false) MultipartFile iconLight) {

        TwinClassUpdateRqDTOv2 request = mapRequest(requestBytes, TwinClassUpdateRqDTOv2.class);
        return processUpdateBatch(request, iconDark, iconLight);
    }

    protected ResponseEntity<? extends Response> processUpdateBatch(TwinClassUpdateRqDTOv2 request, MultipartFile iconDark, MultipartFile iconLight) {
        Response rs = new Response();
        try {
            List<TwinClassUpdate> twinClassUpdates = twinClassUpdateRestDTOReverseMapperV2.convertCollection(request.getTwinClassUpdates());

            twinClassService.updateTwinClasses(twinClassUpdates, convert(iconLight), convert(iconDark));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
