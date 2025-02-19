package org.twins.core.dto.rest.tier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TierCreateRqV1")
public class TierCreateRqDTOv1 extends TierSaveRqDTOv1 {

    @Schema(description = "id", example = DTOExamples.TIER_ID)
    public UUID id;
}