package org.twins.core.controller.rest.priv.twinflow;

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
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowRsDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowUpdateRqDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.UUID;

@Tag(name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWINFLOW_MANAGE, Permissions.TWINFLOW_UPDATE})
public class TwinflowUpdateController extends ApiController {

    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;
    private final TwinflowUpdateRestDTOReverseMapper twinflowUpdateRestDTOReverseMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOMapper;
    private final TwinflowService twinflowService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowUpdateV1", summary = "Update twinflow by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowBaseDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PutMapping(value = "/private/twinflow/{twinflowId}/v1")
    public ResponseEntity<?> twinflowUpdateV1(
            @MapperContextBinding(roots = TwinflowBaseV1RestDTOMapper.class, response = TwinflowRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_ID) @PathVariable UUID twinflowId,
            @RequestBody TwinflowUpdateRqDTOv1 request) {
        TwinflowRsDTOv1 rs = new TwinflowRsDTOv1();
        try {
            I18nEntity nameI18n = i18NSaveRestDTOReverseMapper.convert(request.getNameI18n());
            I18nEntity descriptionsI18n = i18NSaveRestDTOReverseMapper.convert(request.getDescriptionI18n());
            TwinflowEntity twinflowEntity = twinflowUpdateRestDTOReverseMapper.convert(request).setId(twinflowId);
            twinflowEntity = twinflowService.updateTwinflow(twinflowEntity, nameI18n, descriptionsI18n);
            rs
                    .setTwinflow(twinflowBaseV1RestDTOMapper.convert(twinflowEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOMapper.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
