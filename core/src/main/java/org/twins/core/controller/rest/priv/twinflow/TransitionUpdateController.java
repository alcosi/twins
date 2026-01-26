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
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinflow.TransitionUpdateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TransitionUpdateRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionUpdateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TRANSITION)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TRANSITION_MANAGE, Permissions.TRANSITION_UPDATE})
public class TransitionUpdateController extends ApiController {
    private final TransitionUpdateRestDTOReverseMapper transitionUpdateRestDTOReverseMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;
    private final TwinflowTransitionService twinflowTransitionService;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionUpdateV1", summary = "Update transition by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionUpdateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/transition/{transitionId}/v1")
    public ResponseEntity<?> transitionUpdateV1(
            @MapperContextBinding(roots = TransitionBaseV2RestDTOMapper.class, response = TransitionUpdateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_TRANSITION_ID) @PathVariable UUID transitionId,
            @RequestBody TransitionUpdateRqDTOv1 request) {
        TransitionUpdateRsDTOv1 rs = new TransitionUpdateRsDTOv1();
        try {
            I18nEntity nameI18n = i18NSaveRestDTOReverseMapper.convert(request.getNameI18n());
            I18nEntity descriptionsI18n = i18NSaveRestDTOReverseMapper.convert(request.getDescriptionI18n());

            TwinflowTransitionEntity twinflowTransitionEntity = transitionUpdateRestDTOReverseMapper.convert(request);
            twinflowTransitionEntity.setId(transitionId);
            twinflowTransitionEntity = twinflowTransitionService.updateTwinflowTransition(twinflowTransitionEntity, nameI18n, descriptionsI18n);
            rs
                    .setTransition(transitionBaseV2RestDTOMapper.convert(twinflowTransitionEntity, mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }
}
