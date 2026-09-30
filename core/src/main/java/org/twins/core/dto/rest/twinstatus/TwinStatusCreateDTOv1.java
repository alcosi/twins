package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nHasTranslation;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.enums.status.StatusType;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinStatusCreateV1")
public class TwinStatusCreateDTOv1 {

    @NotNull
    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @NotBlank
    @Schema(description = "key within the domain", example = DTOExamples.TWIN_STATUS_KEY)
    public String key;

    @NotNull
    @I18nHasTranslation
    @Schema(description = "name")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "[optional] description")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "[optional] background color hex", example = DTOExamples.COLOR_HEX)
    public String backgroundColor;

    @Schema(description = "[optional] font color hex", example = DTOExamples.COLOR_HEX)
    public String fontColor;

    @Schema(description = "[optional] type")
    public StatusType type;

    @Schema(description = "[optional] inheritable")
    public Boolean inheritable;

    @Schema(description = "[optional] light icon multipart link. Use multipart://<part_name> to reference a file from the same multipart request")
    public String iconLightLink;

    @Schema(description = "[optional] dark icon multipart link. Use multipart://<part_name> to reference a file from the same multipart request")
    public String iconDarkLink;
}
