package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldClauseV1")
public class TwinFieldClauseDTOv1 {
    @Schema(description = "List of conditions. Will be joined with OR operation ")
    public List<TwinFieldConditionDTOv1> conditions;

    public TwinFieldClauseDTOv1 or(TwinFieldConditionDTOv1 condition) {
        conditions = CollectionUtils.safeAdd(conditions, condition);
        return this;
    }
}
