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
    @Schema(description = "List of conditions. Will be joined with OR operation ", type = "object", additionalPropertiesSchema = TwinFieldSearchDTOv1.class, example = """
            {
                "550e8400-e29b-41d4-a716-446655440000": {
                    "type": "TwinFieldSearchNumericV1",
                    "lessThen": "10",
                    "moreThen": "5",
                    "equals": "7"
                },
                "550e8400-e29b-41d4-a716-446655440001": {
                    "type": "TwinFieldSearchTextV1",
                    "valueLikeAllOfList": ["test%"]
                }
            }
            """)
    public List<TwinFieldConditionDTOv1> conditions;

    public TwinFieldClauseDTOv1 or(TwinFieldConditionDTOv1 condition) {
        conditions = CollectionUtils.safeAdd(conditions, condition);
        return this;
    }
}
