package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * A single row of the "decision table" that describes how dependent fields must be changed
 * when a particular combination of base–field rules is satisfied.
 * <p>
 *     The bundle contains two logical parts:
 *     <ul>
 *         <li><b>key</b> – ordered combination of condition descriptors (ConditionKeyDTO). Each element
 *         is represented by {@link TwinClassFieldConditionDTOv1}. All conditions inside the key must evaluate to TRUE
 *         for this bundle to fire.</li>
 *         <li><b>changes</b> – full list of dependent-field changes (DependentChangeDTO) that must be applied
 *         once the key is triggered. Each change is represented by {@link TwinClassFieldRuleDTOv1}.</li>
 *     </ul>
 * </p>
 */
@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldRuleBundleV1")
public class TwinClassFieldRuleBundleDTOv1 {

    @Schema(description = "unique combination of condition descriptors (ConditionKeyDTOs) that triggers the changes")
    public List<TwinClassFieldConditionDTOv1> key;

    @Schema(description = "set of dependent-field changes (DependentChangeDTOs) to apply when the key fires")
    public List<TwinClassFieldRuleDTOv1> changes;
}
