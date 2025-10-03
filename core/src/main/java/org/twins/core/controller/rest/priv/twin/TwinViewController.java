package org.twins.core.controller.rest.priv.twin;

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
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinRsDTOv2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_MANAGE, Permissions.TWIN_VIEW})
public class TwinViewController extends ApiController {
    private final TwinService twinService;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewV2", summary = "Returns twin data by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin/{twinId}/v2")
    public ResponseEntity<?> twinViewV2(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable UUID twinId) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            rs
                    .setTwin(twinRestDTOMapperV2.convert(twinService.findEntitySafe(twinId), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

    @ParametersApiUserHeaders
    @Operation(operationId = "twinViewByAliasV2", summary = "Returns twin data by alias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinRsDTOv2.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_by_alias/{twinAlias}/v2")
    public ResponseEntity<?> twinViewByAliasV2(
            @MapperContextBinding(roots = TwinRestDTOMapperV2.class, response = TwinRsDTOv2.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_ID) @PathVariable String twinAlias) {
        TwinRsDTOv2 rs = new TwinRsDTOv2();
        try {
            rs
                    .setTwin(twinRestDTOMapperV2.convert(twinService.findTwinByAlias(twinAlias), mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
