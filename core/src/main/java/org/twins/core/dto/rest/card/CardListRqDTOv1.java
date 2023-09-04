package org.twins.core.dto.rest.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "CardListRqV1")
public class CardListRqDTOv1 extends Request {
    @Schema(description = "show widgets", example = "true")
    public boolean showWidgets;

}
