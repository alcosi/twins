package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierSearchDTOv1")
public class FactoryMultiplierSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "factory id list")
    public Set<UUID> factoryIdList;

    @Schema(description = "factory id exclude list")
    public Set<UUID> factoryIdExcludeList;

    @Schema(description = "input twin class id list")
    public Set<UUID> inputTwinClassIdList;

    @Schema(description = "input twin class id exclude list")
    public Set<UUID> inputTwinClassIdExcludeList;

    @Schema(description = "multiplier featurer id list")
    public Set<Integer> multiplierFeaturerIdList;

    @Schema(description = "multiplier featurer id exclude list")
    public Set<Integer> multiplierFeaturerIdExcludeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "is active", example = DTOExamples.TERNARY)
    public Ternary active;
}
