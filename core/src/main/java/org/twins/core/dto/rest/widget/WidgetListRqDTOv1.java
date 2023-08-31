package org.twins.core.dto.rest.widget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "WidgetListRqV1")
public class WidgetListRqDTOv1 extends Request {
    @Schema(description = "twin class", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;
}
