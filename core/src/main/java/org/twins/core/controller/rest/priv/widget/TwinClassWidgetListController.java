package org.twins.core.controller.rest.priv.widget;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.widget.WidgetListRsDTOv1;
import org.twins.core.mappers.rest.widget.WidgetRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.widget.WidgetService;

import java.util.UUID;

@Tag(name = ApiTag.WIDGET)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassWidgetListController extends ApiController {
    private final AuthService authService;
    private final WidgetService widgetService;
    private final WidgetRestDTOMapper widgetRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassWidgetListV1", summary = "Returns widget list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Widget list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = WidgetListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/twin_class/{twinClassId}/widget/list/v1", method = RequestMethod.GET)
    public ResponseEntity<?> twinClassWidgetListV1(
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId) {
        WidgetListRsDTOv1 rs = new WidgetListRsDTOv1();
        try {
            rs.widgetList(
                    widgetRestDTOMapper.convertList(
                            widgetService.findWidgets(twinClassId)));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
