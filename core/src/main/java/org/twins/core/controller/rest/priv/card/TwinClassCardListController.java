package org.twins.core.controller.rest.priv.card;

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
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.card.CardListRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.card.CardRestDTOMapper;
import org.twins.core.mappers.rest.card.CardWidgetRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.card.CardService;

import java.util.UUID;

@Tag(description = "Get card list", name = ApiTag.CARD)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TwinClassCardListController extends ApiController {
    private final AuthService authService;
    private final CardService cardService;
    private final CardRestDTOMapper cardRestDTOMapper;

    @ParametersApiUserHeaders
    @Operation(operationId = "twinClassCardListV1", summary = "Returns card list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin card list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CardListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @GetMapping(value = "/private/twin_class/{twinClassId}/card/list/v1")
    public ResponseEntity<?> twinClassCardListV1(
            MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId,
            @RequestParam(name = RestRequestParam.showCardMode, defaultValue = CardRestDTOMapper.Mode._DETAILED) CardRestDTOMapper.Mode showCardMode,
            @RequestParam(name = RestRequestParam.showCardWidgetMode, defaultValue = CardRestDTOMapper.Mode._DETAILED) CardWidgetRestDTOMapper.Mode showCardWidgetMode) {
        CardListRsDTOv1 rs = new CardListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            rs.cardList(
                    cardRestDTOMapper.convertCollection(
                            cardService.findCards(apiUser, twinClassId), mapperContext));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
