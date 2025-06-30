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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.controller.rest.ApiTag;
import org.twins.core.controller.rest.annotation.MapperContextBinding;
import org.twins.core.controller.rest.annotation.ParametersApiUserHeaders;
import org.twins.core.controller.rest.annotation.ProtectedBy;
import org.twins.core.domain.ApiUser;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.card.CardListRsDTOv1;
import org.twins.core.mappers.rest.card.CardRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.card.CardService;
import org.twins.core.service.permission.Permissions;

import java.util.UUID;

@Tag(description = "Get card list", name = ApiTag.CARD)
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@ProtectedBy({Permissions.TWIN_CLASS_CARD_MANAGE, Permissions.TWIN_CLASS_CARD_VIEW})
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
            @MapperContextBinding(roots = CardRestDTOMapper.class, response = CardListRsDTOv1.class) @Schema(hidden = true) MapperContext mapperContext,
            @Parameter(example = DTOExamples.TWIN_CLASS_ID) @PathVariable UUID twinClassId) {
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
