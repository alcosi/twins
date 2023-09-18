package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldValueV2")
public class TwinFieldValueDTOv2 {

    @Schema(description = "field key", example = DTOExamples.TWIN_FIELD_KEY)
    public String key;

    @Schema(description = "field value", example = "")
    public String value;

    @JsonIgnore
    public UUID twinClassId;
}
