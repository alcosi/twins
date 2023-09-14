package org.twins.core.dto.rest.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CardListRsV1")
public class CardListRsDTOv1 extends Response {
    @Schema(description = "results - card list")
    public List<CardDTOv1> cardList;
}
