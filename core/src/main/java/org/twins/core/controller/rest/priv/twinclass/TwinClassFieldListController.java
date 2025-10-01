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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassFieldListRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_MANAGE, Permissions.TWIN_CLASS_VIEW})
public class TwinClassFieldListController extends ApiController {
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassService twinClassService;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    @Deprecated
    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldListV1", summary = "Returns twin class field list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class/{twinClassId}/field/list/v1")
    public ResponseEntity<?> twinClassFieldListV1(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId) {
        TwinClassFieldListRsDTOv1 rs = new TwinClassFieldListRsDTOv1();
        try {
            TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinClassId);
            twinClassFieldService.loadTwinClassFields(twinClassEntity);
            rs
                    .twinClassFieldList(twinClassFieldRestDTOMapper.convertCollection(twinClassEntity.getTwinClassFieldKit().getCollection(), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldViewV1", summary = "Returns twin class field list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field information", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class_field/{twinClassFieldId}/v1")
    public ResponseEntity<?> twinClassFieldViewV1(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_FIELD_ID) @PathVariable UUID twinClassFieldId) {
        TwinClassFieldRsDTOv1 rs = new TwinClassFieldRsDTOv1();
        try {
            TwinClassFieldEntity twinClassFieldsList = twinClassFieldService.findEntitySafe(twinClassFieldId);
            rs
                    .field(twinClassFieldRestDTOMapper.convert(twinClassFieldsList, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));;
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldViewByKeyV1", summary = "Returns twin class field list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field information", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class_by_key/{classKey}/field/{fieldKey}/v1")
    public ResponseEntity<?> twinClassFieldViewByKeyV1(
            @MapperContextBinding(roots = TwinClassFieldRestDTOMapper.class, response = TwinClassFieldRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_KEY) @PathVariable String classKey,
            @Parameter(example = DTOExamples.TWIN_FIELD_KEY) @PathVariable String fieldKey) {
        TwinClassFieldRsDTOv1 rs = new TwinClassFieldRsDTOv1();
        try {
            TwinClassFieldEntity twinClassFieldsList = twinClassFieldService.findByTwinClassKeyAndKey(classKey, fieldKey);
            rs.field(twinClassFieldRestDTOMapper.convert(twinClassFieldsList, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
