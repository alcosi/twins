package org.twins.face.dto.rest.twidget.tw002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTW002AccordionItemV1")
public class FaceTW002AccordionItemDTOv1 {
    @Schema(description = "item id", example = DTOExamples.FACE_ID)
    public UUID id;

    @Schema(description = "locale")
    public String locale;

    @Schema(description = "label for header")
    public String label;
}
