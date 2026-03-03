package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinFieldsFilterV1")
public class TwinFieldsFilterDTOv1 {
    @Schema(description = "List of clauses. Will be joined by logic AND operation")
    public List<TwinFieldClauseDTOv1> clauses;

    public TwinFieldsFilterDTOv1 and(TwinFieldClauseDTOv1 clause) {
        CollectionUtils.safeAdd(clauses, clause);
        return this;
    }
}
