package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "ActionRestrictionReasonCreateV1")
public class ActionRestrictionReasonCreateDTOv1 {

    @NotBlank
    @Schema(description = "type")
    public String type;

    @Schema(description = "description i18n")
    public I18nSaveDTOv1 descriptionI18n;
}
