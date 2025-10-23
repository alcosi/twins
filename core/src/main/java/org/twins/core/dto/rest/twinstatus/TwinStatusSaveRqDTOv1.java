package org.twins.core.dto.rest.twinstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.enums.status.StatusType;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinStatusSaveRqV1")
public class TwinStatusSaveRqDTOv1 extends Request {
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

    @JsonIgnore
    public UUID twinClassId;
}
