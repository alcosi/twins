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
import org.twins.core.dto.rest.twinflow.TransitionCreateRqDTOv1;
import org.twins.core.dto.rest.twinflow.TransitionCreateRsDTOv1;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TransitionCreateRestDTOReverseMapper;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.UUID;

@Tag(description = "", name = ApiTag.TWINFLOW)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TRANSITION_MANAGE, Permissions.TRANSITION_CREATE})
public class TransitionCreateController extends ApiController {
    private final TransitionCreateRestDTOReverseMapper transitionCreateRestDTOReverseMapper;
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;
    private final TwinflowTransitionService twinflowTransitionService;
    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "transitionCreateV1", summary = "Create new transition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transition data", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = TransitionCreateRsDTOv1.class))}),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @PostMapping(value = "/private/twinflow/{twinflowId}/transition/v1")
    public ResponseEntity<?> transitionCreateV1(
            @MapperContextBinding(roots = TransitionBaseV2RestDTOMapper.class, response = TransitionCreateRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWINFLOW_ID) @PathVariable UUID twinflowId,
            @RequestBody TransitionCreateRqDTOv1 request) {
        TransitionCreateRsDTOv1 rs = new TransitionCreateRsDTOv1();
        try {
            request.getTransition().setTwinflowId(twinflowId);
            I18nEntity nameI18n = i18NSaveRestDTOReverseMapper.convert(request.getTransition().getNameI18n());
            I18nEntity descriptionsI18n = i18NSaveRestDTOReverseMapper.convert(request.getTransition().getDescriptionI18n());

            TwinflowTransitionEntity twinflowTransitionEntity = transitionCreateRestDTOReverseMapper.convert(request.getTransition());
            twinflowTransitionEntity = twinflowTransitionService.createTwinflowTransition(twinflowTransitionEntity, nameI18n, descriptionsI18n);
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
