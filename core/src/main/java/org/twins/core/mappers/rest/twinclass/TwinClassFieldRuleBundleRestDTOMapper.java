package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleBundleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldRuleBundleMode;

import java.util.*;

/**
 * Converts a collection of {@link TwinClassFieldRuleEntity} objects (with eagerly loaded conditions)
 * to a list of {@link TwinClassFieldRuleBundleDTOv1} that represent a decision table describing how
 * dependent fields must be changed when a certain combination of base-field rules is satisfied.
 * <p>
 * The conversion algorithm replicates (and replaces) the logic that previously lived in
 * {@code TwinClassFieldRuleService#buildRuleBundles}. The steps are:
 * <ol>
 *     <li>Iterate over every rule and build its DTO representation (DependentChange).</li>
 *     <li>For each condition inside the rule calculate a <b>condition-key hash</b>
 *         (base field + operator + evaluator params).</li>
 *     <li>Group by this key hashing – each unique hash becomes a bundle key.</li>
 *     <li>Populate the bundle:
 *          <ul>
 *              <li>Add the rule descriptor to {@link TwinClassFieldRuleBundleDTOv1#key} (duplicates avoided).</li>
 *              <li>Add the condition DTO to {@link TwinClassFieldRuleBundleDTOv1#changes}.</li>
 *          </ul>
 *     </li>
 * </ol>
 * The resulting map is finally transformed into an ordered list preserving the insertion order.
 */
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldRuleBundleMode.class)
public class TwinClassFieldRuleBundleRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldRuleEntity, TwinClassFieldRuleBundleDTOv1> {

    private final TwinClassFieldRuleRestDTOMapper twinClassFieldRuleRestDTOMapper;
    private final TwinClassFieldConditionRestDTOMapper twinClassFieldConditionRestDTOMapper;

    /**
     * We don’t support one-to-one mapping for rule-bundle because a bundle is formed from
     * multiple rule/condition entities. For any accidental direct call we throw
     * {@link UnsupportedOperationException}.
     */
    @Override
    public void map(TwinClassFieldRuleEntity src, TwinClassFieldRuleBundleDTOv1 dst, MapperContext mapperContext) {
        throw new UnsupportedOperationException("TwinClassFieldRuleBundleRestDTOMapper works on collections – use convertCollection()");
    }

    /**
     * Custom conversion that transforms a collection of rules to a list of bundles.
     */
    @Override
    public List<TwinClassFieldRuleBundleDTOv1> convertCollection(Collection<TwinClassFieldRuleEntity> rules,
                                                                 MapperContext mapperContext) throws Exception {
        if (CollectionUtils.isEmpty(rules))
            return Collections.emptyList();

        Map<String, TwinClassFieldRuleBundleDTOv1> bundleMap = new LinkedHashMap<>();

        for (TwinClassFieldRuleEntity rule : rules) {
            // Build DTO for the rule itself (DependentChange)
            var ruleDto = twinClassFieldRuleRestDTOMapper.convert(rule, mapperContext);

            // Iterate through conditions to build keys
            if (CollectionUtils.isNotEmpty(rule.getConditions())) {
                for (TwinClassFieldConditionEntity cond : rule.getConditions()) {
                    String keyHash = buildConditionKeyHash(cond);
                    TwinClassFieldRuleBundleDTOv1 bundle = bundleMap.computeIfAbsent(keyHash, k -> new TwinClassFieldRuleBundleDTOv1()
                            .setKey(new ArrayList<>())
                            .setChanges(new ArrayList<>()));

                    // add the condition descriptor to the key set (avoid duplicates)
                    var condDto = twinClassFieldConditionRestDTOMapper.convert(cond, mapperContext);
                    boolean condExists = bundle.getKey().stream().anyMatch(c -> Objects.equals(c.id, condDto.id));
                    if (!condExists)
                        bundle.getKey().add(condDto);

                    // add rule descriptor to changes (avoid duplicates)
                    boolean ruleExists = bundle.getChanges().stream().anyMatch(r -> Objects.equals(r.id, ruleDto.id));
                    if (!ruleExists)
                        bundle.getChanges().add(ruleDto);
                }
            }
        }

        return new ArrayList<>(bundleMap.values());
    }

    private String buildConditionKeyHash(TwinClassFieldConditionEntity cond) {
        return cond.getBaseTwinClassFieldId() + "|" + cond.getConditionOperator() + "|" + cond.getConditionEvaluatorParams();
    }

    @Override
    public String getObjectCacheId(TwinClassFieldRuleEntity src) {
        // We don’t cache individual rules here because the mapper works with collections.
        return null;
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        // Currently we always expose rule-bundles
        return false;
    }
}
