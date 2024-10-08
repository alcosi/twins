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
import org.cambium.i18n.dao.I18nEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.TwinflowCreateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowCreateRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.related.RelatedObjectsRestDTOConverter;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowCreateRestDTOReverseMapper;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWIN_CLASS)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinflowCreateController extends ApiController {

    private final TwinflowBaseV2RestDTOMapper twinflowBaseV2RestDTOMapper;
    private final TwinflowCreateRestDTOReverseMapper twinflowCreateRestDTOReverseMapper;
    private final I18nRestDTOReverseMapper i18nRestDTOReverseMapper;
    private final RelatedObjectsRestDTOConverter relatedObjectsRestDTOConverter;

    private final TwinflowService twinflowService;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinflowCreateV1", summary = "Create new twinflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twinflow data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TwinflowCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twin_class/{twinClassId}/twinflow/v1")
    public ResponseEntity<?> twinflowCreateV1(
            @MapperContextBinding(roots = TwinflowBaseV2RestDTOMapper.class, response = TwinflowCreateRsDTOv1.class) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestBody TwinflowCreateRqDTOv1 request) {
        TwinflowCreateRsDTOv1 rs = new TwinflowCreateRsDTOv1();
        try {
            I18nEntity nameI18n = i18nRestDTOReverseMapper.convert(request.getNameI18n());
            I18nEntity descriptionsI18n = i18nRestDTOReverseMapper.convert(request.getDescriptionI18n());

            TwinflowEntity twinflowEntity = twinflowCreateRestDTOReverseMapper.convert(request).setTwinClassId(twinClassId);
            twinflowEntity = twinflowService.createTwinflow(twinflowEntity, nameI18n, descriptionsI18n);
            rs
                    .setTwinflow(twinflowBaseV2RestDTOMapper.convert(twinflowEntity, mapperContext))
                    .setRelatedObjects(relatedObjectsRestDTOConverter.convert(mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
