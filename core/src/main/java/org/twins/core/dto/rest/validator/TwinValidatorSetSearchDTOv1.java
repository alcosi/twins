package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSetSearchV1")
public class TwinValidatorSetSearchDTOv1 {
    @Schema(description = "idList")
    public Set<UUID> idList;

    @Schema(description = "idExcludeList")
    public Set<UUID> idExcludeList;

    @Schema(description = "nameLikeList")
    public Set<String> nameLikeList;

    @Schema(description = "nameNotLikeList")
    public Set<String> nameNotLikeList;

    @Schema(description = "descriptionLikeList")
    public Set<String> descriptionLikeList;

    @Schema(description = "descriptionNotLikeList")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "invert", example = DTOExamples.TERNARY)
    public Ternary invert;

    @Schema(description = "usageCountRange")
    public IntegerRange usageCountRange;
}
