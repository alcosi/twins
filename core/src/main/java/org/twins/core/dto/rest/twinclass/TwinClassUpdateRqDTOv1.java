package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ReplaceOperationDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassUpdateRqV1")
public class TwinClassUpdateRqDTOv1 extends TwinClassSaveRqDTOv1 {
    @Schema(description = "[optional] if marker data list is changed during update, " +
            "you should specify what should be done with already existed markers")
    public ReplaceOperationDTOv1 markersReplace;

    @Schema(description = "[optional] if tag data list is changed during update, " +
            "you should specify what should be done with already existed tags")
    public ReplaceOperationDTOv1 tagsReplace;

    @JsonIgnore
    public UUID twinClassId;
}
