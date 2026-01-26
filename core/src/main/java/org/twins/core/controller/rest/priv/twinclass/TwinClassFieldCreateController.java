package org.twins.core.controller.rest.priv.twinclass;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twinclass.TwinClassFieldSave;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.*;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldCreateRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;
import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_UPDATE})
public class TwinClassFieldCreateController extends ApiController {
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final TwinClassFieldCreateRestDTOReverseMapper twinClassFieldCreateRestDTOReverseMapper;
    private final TwinClassFieldCreateRestDTOReverseMapperV2 twinClassFieldCreateRestDTOReverseMapperV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldCreateV1", summary = "Create new twin class field")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/{twinClassId}/field/v1")
    public ResponseEntity<?> twinClassFieldCreateV1(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestBody TwinClassFieldCreateRqDTOv1 request) {
        TwinClassFieldCreateRsDTOv1 rs = new TwinClassFieldCreateRsDTOv1();
        try {
            TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.createFields(twinClassFieldCreateRestDTOReverseMapper.convert(request.setTwinClassId(twinClassId)));

            rs
                    .setField(twinClassFieldRestDTOMapper.convert(twinClassFieldEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldCreateV2", summary = "Create batch twin class fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class fields created successfully", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldCreateRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field/v1")
    public ResponseEntity<?> twinClassFieldCreateV2(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldCreateRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassFieldCreateRqDTOv2 request) {
        TwinClassFieldCreateRsDTOv2 rs = new TwinClassFieldCreateRsDTOv2();
        try {
            List<TwinClassFieldSave> twinClassFieldSaves = twinClassFieldCreateRestDTOReverseMapperV2.convertCollection(request.getTwinClassFields());
            var twinClassFields = twinClassFieldService.createFields(twinClassFieldSaves);
            rs
                    .setFields(twinClassFieldRestDTOMapper.convertCollection(twinClassFields, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
