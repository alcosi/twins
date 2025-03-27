package org.twins.face.dto.rest.twidget;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceTW002v1", description = "Twin i18n field accordion widget")
public class FaceTW002DTOv1 extends FaceTwidgetDTOv1 {
    @Schema(description = "uniq key")
    public String key;

    @Schema(description = "label for widget")
    public String label;

    @Schema(description = "widget should display an accordion with translations for given field [by id]")
    public UUID i18nTwinClassFieldId;

    @Schema(description = "")
    public List<FaceTW002AccordionItemDTOv1> accordionItems;
}
