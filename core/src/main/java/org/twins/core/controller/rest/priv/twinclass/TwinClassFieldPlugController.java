package org.twins.core.controller.rest.priv.twinclass;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twinclass.TwinClassFieldPlugEntity;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinclass.TwinClassFieldPlugRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldPlugRsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldUnplugRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldPlugRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldPlugRestDTOReverseMapper;
import org.twins.core.service.twinclass.TwinClassFieldPlugService;

import java.util.List;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
//@ProtectedBy(Permissions.TWIN_CLASS_FIELD_PLUG_MANAGE)
public class TwinClassFieldPlugController extends ApiController {

    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinClassFieldPlugService twinClassFieldPlugService;
    private final TwinClassFieldPlugRestDTOMapper twinClassFieldPlugRestDTOMapper;
    private final TwinClassFieldPlugRestDTOReverseMapper twinClassFieldPlugRestDTOReverseMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldPlugV1", summary = "Plug field to twin class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plugged data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinClassFieldPlugRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field/plug/v1")
    public ResponseEntity<?> twinClassFieldPlugV1(
            @MapperContextBinding(roots = TwinClassFieldPlugRestDTOMapper.class, response = TwinClassFieldPlugRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @RequestBody TwinClassFieldPlugRqDTOv1 request) {
        TwinClassFieldPlugRsDTOv1 rs = new TwinClassFieldPlugRsDTOv1();
        try {
            List<TwinClassFieldPlugEntity> pluggedFields = twinClassFieldPlugService.plugFields(twinClassFieldPlugRestDTOReverseMapper.convertCollection(request.getFieldsToPlug()));

            rs
                    .setPluggedFields(twinClassFieldPlugRestDTOMapper.convertCollection(pluggedFields, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassFieldUnplugV1", summary = "Unplug field to twin class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unplugging field result", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Response.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class_field/unplug/v1")
    public ResponseEntity<?> twinClassFieldPlugV1(@RequestBody TwinClassFieldUnplugRqDTOv1 request) {
        Response rs = new Response();

        try {
            List<TwinClassFieldPlugEntity> fieldsToUnplug = twinClassFieldPlugRestDTOReverseMapper.convertCollection(request.getFieldsToUnplug());

            twinClassFieldPlugService.unplugFields(fieldsToUnplug);
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }

        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
