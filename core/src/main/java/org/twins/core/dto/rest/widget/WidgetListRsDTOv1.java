package org.twins.core.dto.rest.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.card.CardDTOv1;

import java.util.List;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "WidgetListRsV1")
public class WidgetListRsDTOv1 extends Response {
    @Schema(description = "results - valid widget list")
    public List<WidgetDTOv1> widgetList;
}
