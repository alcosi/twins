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
@Schema(name = "FactoryConditionSetSearchDTOv1")
public class FactoryConditionSetSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin factory id list")
    public Set<UUID> twinFactoryIdList;

    @Schema(description = "twin factory id exclude list")
    public Set<UUID> twinFactoryIdExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name like exclude list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description like exclude list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "cachable", example = DTOExamples.TERNARY)
    public Ternary cachable;
}
