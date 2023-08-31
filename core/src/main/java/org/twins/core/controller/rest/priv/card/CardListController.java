package org.twins.core.controller.rest.priv.card;

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
import org.twins.core.dto.rest.card.CardListRqDTOv1;
import org.twins.core.dto.rest.card.CardListRsDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.card.CardRestDTOMapper;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.card.CardService;

@Tag(description = "Get card list", name = "card")
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CardListController extends ApiController {
    private final AuthService authService;
    private final CardService cardService;
    private final CardRestDTOMapper cardRestDTOMapper;

    @Operation(operationId = "cardListV1", summary = "Returns card list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Twin card list prepared", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CardListRsDTOv1.class)) }),
            @ApiResponse(responseCode = "401", description = "Access is denied")})
    @RequestMapping(value = "/private/card/v1", method = RequestMethod.POST)
    public ResponseEntity<?> twinClassListV1(
            @Parameter(name = "UserId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.USER_ID) String userId,
            @Parameter(name = "DomainId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.DOMAIN_ID) String domainId,
            @Parameter(name = "BusinessAccountId", in = ParameterIn.HEADER,  required = true, example = DTOExamples.BUSINESS_ACCOUNT_ID) String businessAccountId,
            @Parameter(name = "Channel", in = ParameterIn.HEADER,  required = true, example = DTOExamples.CHANNEL) String channel,
            @RequestBody CardListRqDTOv1 request) {
        CardListRsDTOv1 rs = new CardListRsDTOv1();
        try {
            ApiUser apiUser = authService.getApiUser();
            MapperProperties mapperProperties = MapperProperties.create();
            if (request.showWidgets())
                mapperProperties.setMode(CardRestDTOMapper.Mode.SHOW_WIDGETS);
            rs.cardList(
                    cardRestDTOMapper.convertList(
                            cardService.findCards(apiUser, request.twinClassId()), mapperProperties));
        } catch (ServiceException se) {
            return createErrorRs(se, rs);
        } catch (Exception e) {
            return createErrorRs(e, rs);
        }
        return new ResponseEntity<>(rs, HttpStatus.OK);
    }

}
