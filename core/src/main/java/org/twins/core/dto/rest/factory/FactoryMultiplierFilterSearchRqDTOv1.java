package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryMultiplierFilterSearchRqV1")
public class FactoryMultiplierFilterSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "factory id list")
    public Set<UUID> factoryIdList;

    @Schema(description = "factory id exclude list")
    public Set<UUID> factoryIdExcludeList;

    @Schema(description = "factory multiplier id list")
    public Set<UUID> factoryMultiplierIdList;

    @Schema(description = "factory multiplier id exclude list")
    public Set<UUID> factoryMultiplierIdExcludeList;

    @Schema(description = "input twin class id list")
    public Set<UUID> inputTwinClassIdList;

    @Schema(description = "input twin class id exclude list")
    public Set<UUID> inputTwinClassIdExcludeList;

    @Schema(description = "factory condition set id list")
    public Set<UUID> factoryConditionSetIdList;

    @Schema(description = "factory condition set id exclude list")
    public Set<UUID> factoryConditionSetIdExcludeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;

    @Schema(description = "factory condition invert")
    public Ternary factoryConditionInvert;
}
