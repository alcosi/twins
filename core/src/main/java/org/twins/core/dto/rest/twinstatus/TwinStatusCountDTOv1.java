package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.enums.status.StatusType;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinStatusCountV1")
public class TwinStatusCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twin class id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "inheritable")
    public Boolean inheritable;

    @Schema(description = "status type")
    public StatusType type;
}
