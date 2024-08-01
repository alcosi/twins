package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldUpdateRqV1")
public class TwinClassFieldUpdateRqDTOv1 extends TwinClassFieldSaveRqDTOv1 {
    @JsonIgnore
    public UUID twinClassFieldId;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;
}
