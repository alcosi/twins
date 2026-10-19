package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSearchV1")
public class TwinValidatorSearchDTOv1 {
    @Schema(description = "idList")
    public Set<UUID> idList;

    @Schema(description = "idExcludeList")
    public Set<UUID> idExcludeList;

    @Schema(description = "twinValidatorSetIdList")
    public Set<UUID> twinValidatorSetIdList;

    @Schema(description = "twinValidatorSetIdExcludeList")
    public Set<UUID> twinValidatorSetIdExcludeList;

    @Schema(description = "validatorFeaturerIdList")
    public Set<Integer> validatorFeaturerIdList;

    @Schema(description = "validatorFeaturerIdExcludeList")
    public Set<Integer> validatorFeaturerIdExcludeList;

    @Schema(description = "descriptionLikeList")
    public Set<String> descriptionLikeList;

    @Schema(description = "descriptionNotLikeList")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "invert", example = DTOExamples.TERNARY)
    public Ternary invert;

    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;
}
