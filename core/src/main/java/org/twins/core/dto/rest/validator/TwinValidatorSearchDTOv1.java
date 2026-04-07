package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.IntegerRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinValidatorSearchV1")
public class TwinValidatorSearchDTOv1 {
    @Schema(description = "twin validator id list")
    public Set<UUID> idList;

    @Schema(description = "twin validator id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin validator set id list")
    public Set<UUID> twinValidatorSetIdList;

    @Schema(description = "twin validator set id exclude list")
    public Set<UUID> twinValidatorSetIdExcludeList;

    @Schema(description = "validator featurer id list")
    public Set<Integer> validatorFeaturerIdList;

    @Schema(description = "validator featurer id exclude list")
    public Set<Integer> validatorFeaturerIdExcludeList;

    @Schema(description = "invert")
    public Ternary invert;

    @Schema(description = "active")
    public Ternary active;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "order range")
    public IntegerRangeDTOv1 order;
}
