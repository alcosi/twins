package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.enums.status.StatusType;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinStatusUpdateV1")
public class TwinStatusUpdateDTOv1 {

    @NotNull
    @Schema(description = "twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID id;

    @Schema(description = "[optional] key within the domain", example = DTOExamples.TWIN_STATUS_KEY)
    public String key;

    @Schema(description = "[optional] name")
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
