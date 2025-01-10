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
@Schema(name =  "FactoryBranchSearchRqV1")
public class FactoryBranchSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "factory id list")
    public Set<UUID> factoryIdList;

    @Schema(description = "factory id exclude list")
    public Set<UUID> factoryIdExcludeList;

    @Schema(description = "factory condition set id list")
    public Set<UUID> factoryConditionSetIdList;

    @Schema(description = "factory condition set id exclude list")
    public Set<UUID> factoryConditionSetIdExcludeList;

    @Schema(description = "next factory id list")
    public Set<UUID> nextFactoryIdList;

    @Schema(description = "next factory id exclude list")
    public Set<UUID> nextFactoryIdExcludeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;
}
