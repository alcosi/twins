package org.twins.core.dto.rest.twinstatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinStatusSaveRsV1")
public class TwinStatusSaveRsDTOv1 extends Response {

    @Schema(description = "[optional] key within the domain", example = DTOExamples.TWIN_STATUS_KEY)
    public String key;

    @Schema(description = "[optional] name", example = DTOExamples.TWIN_STATUS_NAME)
    public String name;

    @Schema(description = "[optional] description", example = DTOExamples.TWIN_STATUS_DESCRIPTION)
    public String description;

    @Schema(description = "[optional] url for status UI logo", example = "https://twins.org/img/twin_status_default.png")
    public String logo;

    @Schema(description = "[optional] color hex", example = "#ff00ff")
    public String color;

    @JsonIgnore
    public UUID twinClassId;
}
