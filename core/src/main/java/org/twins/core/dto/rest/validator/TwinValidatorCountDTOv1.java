package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorCountV1")
public class TwinValidatorCountDTOv1 extends CountDTOv1 {
    @Schema(description = "invert")
    public Boolean invert;

    @Schema(description = "active")
    public Boolean active;

    @Schema(description = "twin validator set id")
    @RelatedObject(type = TwinValidatorSetDTOv1.class, name = "twinValidatorSet")
    public UUID twinValidatorSetId;

    @Schema(description = "validator featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "validatorFeaturer")
    public Integer validatorFeaturerId;
}
