package org.twins.core.dto.rest.twinstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinStatusSaveRqV1")
public class TwinStatusSaveRqDTOv1 extends Request {
    @Schema(description = "[optional] key within the domain", example = DTOExamples.TWIN_STATUS_KEY)
    public String key;

    @Schema(description = "[optional] name")
    public I18nDTOv1 nameI18n;

    @Schema(description = "[optional] description")
    public I18nDTOv1 descriptionI18n;

    @Schema(description = "[optional] url for status UI logo", example = "https://twins.org/img/twin_status_default.png")
    public String logo;

    @Schema(description = "[optional] color hex", example = "#ff00ff")
    public String color;

    @JsonIgnore
    public UUID twinClassId;
}
