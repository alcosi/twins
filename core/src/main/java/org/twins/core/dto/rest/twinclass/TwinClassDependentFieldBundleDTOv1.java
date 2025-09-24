package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Represents a single row of the decision table that describes how **this** field (dependent)
 * should be updated depending on values / parameters of other (base) fields.
 * <p>
 * The bundle is built around two logical parts:
 * <ul>
 *     <li><b>key</b> – a unique descriptor of the change that will be applied to the dependent
 *     field. Technically it is expressed by {@link TwinClassFieldRuleDTOv1} where only the
 *     change-related attributes are relevant (targetElement, targetParamKey, dependentOverwrittenValue,
 *     dependentOverwrittenDatalistId, rulePriority). Multiple rules that lead to the same change are
 *     collapsed into a single bundle keyed by this descriptor.</li>
 *     <li><b>conditions</b> – the full list of atomic conditions on base fields that may trigger
 *     the change. Each element is represented by {@link TwinClassFieldConditionDTOv1}.</li>
 * </ul>
 * </p>
 */
@Data
@Accessors(chain = true)
@Schema(name = "TwinClassDependentFieldBundleV1")
public class TwinClassDependentFieldBundleDTOv1 {

    @Schema(description = "descriptor of the change that will be written to the dependent field when conditions are satisfied")
    public TwinClassFieldRuleDTOv1 key;

    @Schema(description = "list of atomic conditions (across all rules) that may trigger the change")
    public List<TwinClassFieldConditionDTOv1> conditions;
}
