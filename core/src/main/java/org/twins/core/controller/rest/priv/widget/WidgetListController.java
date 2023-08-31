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
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.widget.WidgetListRqDTOv1;
import org.twins.core.dto.rest.widget.WidgetListRsDTOv1;
import org.twins.core.mappers.rest.widget.WidgetRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.widget.WidgetService;

@Tag(description = "Get widget list", name = "widget")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class WidgetListController extends ApiController {
    private final AuthService authService;
    private final WidgetService widgetService;
    private final WidgetRestDTOMapper widgetRestDTOMapper;

    @Operation(operationId = "cardListV1", summary = "Returns widget list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Widget list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = WidgetListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/widget/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinClassListV1(
            @Parameter(name = "UserId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.USER_ID) String userId,
            @Parameter(name = "DomainId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.DOMAIN_ID) String domainId,
            @Parameter(name = "BusinessAccountId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.BUSINESS_ACCOUNT_ID) String businessAccountId,
            @Parameter(name = "Channel", in = ParameterIn.HEADER,  required = true, example = DTOExamples.CHANNEL) String channel,
            @RequestBody WidgetListRqDTOv1 request) {
        WidgetListRsDTOv1 rs = new WidgetListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.widgetList(
                    widgetRestDTOMapper.convertList(
                            widgetService.findWidgets(request.twinClassId())));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
