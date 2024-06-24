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
import org.twins.core.controller.rest.RestRequestParam;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassFieldListRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;
import java.util.UUID;

@Tag(name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassFieldListController extends ApiController {
    private final AuthService authService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldListV1", summary = "Returns twin class field list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin class field list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldListRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/{twinClassId}/field/list/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassFieldListV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode) {
        TwinClassFieldListRsDTOv1 rs = new TwinClassFieldListRsDTOv1();
        try {
            MapperContext mapperContext = new MapperContext()
                    .setMode(showClassFieldMode);
            List<TwinClassFieldEntity> twinClassFieldsList = twinClassFieldService.findTwinClassFields(twinClassId);
            rs.twinClassFieldList(twinClassFieldRestDTOMapper.convertCollection(twinClassFieldsList, mapperContext));
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
    @RequestMapping(value = "/private/twin_class_field/{twinClassFieldId}/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassFieldViewV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_FIELD_ID) @PathVariable UUID twinClassFieldId,
            @RequestParam(name = RestRequestParam.showClassFieldMode, defaultValue = TwinClassFieldRestDTOMapper.Mode._SHORT) TwinClassFieldRestDTOMapper.Mode showClassFieldMode) {
        TwinClassFieldRsDTOv1 rs = new TwinClassFieldRsDTOv1();
        try {
            TwinClassFieldEntity twinClassFieldsList = twinClassFieldService.findEntitySafe(twinClassFieldId);
            MapperContext mapperContext = new MapperContext()
                    .setMode(showClassFieldMode);
            rs.field(twinClassFieldRestDTOMapper.convert(twinClassFieldsList, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
