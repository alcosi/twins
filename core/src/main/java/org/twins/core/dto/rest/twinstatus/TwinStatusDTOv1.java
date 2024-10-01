package org.twins.core.dto.rest.twinstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatusV1")
public class TwinStatusDTOv1 {
    @Schema(description = "uuid", example = DTOExamples.TWIN_STATUS_ID)
    public UUID id;

    @Schema(description = "key within the domain")
    public String key;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "url for status UI logo", example = "https://twins.org/img/twin_status_default.png")
    public String logo;

    @Schema(description = "background color hex", example = DTOExamples.COLOR_HEX)
    public String backgroundColor;

    @Schema(description = "font color hex", example = DTOExamples.COLOR_HEX)
    public String fontColor;

    @JsonIgnore
    public UUID twinClassId;
}
