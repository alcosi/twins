package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerCountV1")
public class TwinTriggerCountDTOv1 extends CountDTOv1 {
    @Schema(description = "trigger featurer id", example = DTOExamples.FEATURER_ID)
    @RelatedObject(type = FeaturerDTOv1.class, name = "triggerFeaturer")
    public Integer triggerFeaturerId;

    @Schema(description = "is active")
    public Boolean active;

    @Schema(description = "job twin class id")
    @RelatedObject(type = TwinClassDTOv1.class, name = "jobTwinClass")
    public UUID jobTwinClassId;
}
