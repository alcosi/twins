package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "ActionRestrictionReasonSaveV1")
public class ActionRestrictionReasonSaveDTOv1 {
    @Schema(description = "type")
    public String type;

    @Schema(description = "description i18n")
    public I18nSaveDTOv1 descriptionI18n;
}
