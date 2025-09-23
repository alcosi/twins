package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
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
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRqDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassCreateRsDTOv2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassCreateRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.List;

import static org.cambium.common.util.MultipartFileUtils.convert;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_CREATE})
public class TwinClassCreateController extends ApiController {
    private final TwinClassService twinClassService;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final TwinClassCreateRestDTOReverseMapper twinClassCreateRestDTOReverseMapper;
    private final TwinClassCreateRestDTOReverseMapperV2 twinClassCreateRestDTOReverseMapperV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassCreateV1", summary = "Create new twin class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/v1")
    public ResponseEntity<?> twinClassCreateV1(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassCreateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassCreateRqDTOv1 request) {
        TwinClassCreateRsDTOv1 rs = new TwinClassCreateRsDTOv1();
        try {
            TwinClassEntity twinClassEntity = twinClassService.createInDomainClass(twinClassCreateRestDTOReverseMapper.convert(request), null, null);
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
    @Operation(operationId = "twinClassCreateV2", summary = "Create twin classes batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin classes created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassCreateRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/v2")
    public ResponseEntity<?> twinClassCreateV2(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassCreateRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassCreateRqDTOv2 request) {
        return processBatch(request, mapperContext, null, null);
    }

    @SneakyThrows
    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassCreateV2Multipart", summary = "Create twin classes batch with icons")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin classes created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassCreateRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> twinClassCreateV2Multipart(
            @MapperContextBinding(roots = TwinClassRestDTOMapper.class, response = TwinClassCreateRsDTOv2.class)
            @Schema(hidden = true) MapperContext mapperContext,
            @Schema(implementation = TwinClassCreateRqDTOv2.class, requiredMode = Schema.RequiredMode.REQUIRED)
            @RequestPart("request") byte[] requestBytes,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Dark icon for all twin classes")
            @RequestPart(required = false) MultipartFile iconDark,
            @Schema(implementation = MultipartFile.class, requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Light icon for all twin classes")
            @RequestPart(required = false) MultipartFile iconLight) {

        TwinClassCreateRqDTOv2 request = mapRequest(requestBytes, TwinClassCreateRqDTOv2.class);
        return processBatch(request, mapperContext, iconDark, iconLight);
    }

    protected ResponseEntity<? extends Response> processBatch(TwinClassCreateRqDTOv2 request, MapperContext mapperContext, MultipartFile iconDark, MultipartFile iconLight) {
        TwinClassCreateRsDTOv2 rs = new TwinClassCreateRsDTOv2();
        try {
            List<TwinClassEntity> twinClassEntityList = twinClassService.createInDomainClass(twinClassCreateRestDTOReverseMapperV2.convertCollection(request.getTwinClassCreates()), convert(iconLight), convert(iconDark));
            rs
                    .setTwinClassList(twinClassRestDTOMapper.convertCollection(twinClassEntityList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
