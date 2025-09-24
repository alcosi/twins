package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDependentFieldBundleDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassDependentFieldBundleMode;

import java.util.*;

/**
 * Builds a collection of {@link TwinClassDependentFieldBundleDTOv1} for a dependent field,
 * based on the collection of {@link TwinClassFieldRuleEntity} in which the field participates
 * as <i>dependent</i>.
 * <p>
 * Algorithm:
 * <ol>
 *     <li>Iterate over provided rules (they must contain eager-loaded conditions).</li>
 *     <li>For every rule build its DTO (only change-descriptor attributes are meaningful here
 *     so the full rule DTO is used as a key).</li>
 *     <li>Group by the change descriptor – each unique descriptor becomes a decision bundle.</li>
 *     <li>For each bundle accumulate all <b>unique</b> condition DTOs from rules that share the descriptor.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassDependentFieldBundleMode.class) // reuse rule modes for exposure control
public class TwinClassDependentFieldRestDTOMapperV1 extends RestSimpleDTOMapper<TwinClassFieldRuleEntity, TwinClassDependentFieldBundleDTOv1> {

    private final TwinClassFieldRuleRestDTOMapper ruleMapper;
    private final TwinClassFieldConditionRestDTOMapper conditionMapper;

    /**
     * Not used – conversion relies on collection processing.
     */
    @Override
    public void map(TwinClassFieldRuleEntity src, TwinClassDependentFieldBundleDTOv1 dst, MapperContext mapperContext) {
        throw new UnsupportedOperationException("TwinClassDependentFieldRestDTOMapperV1 works on collections – use convertCollection()");
    }

    /**
     * Converts the list of rules for a single dependent field into bundles.
     */
    @Override
    public List<TwinClassDependentFieldBundleDTOv1> convertCollection(Collection<TwinClassFieldRuleEntity> rules,
                                                                      MapperContext mapperContext) throws Exception {
        if (CollectionUtils.isEmpty(rules))
            return Collections.emptyList();

        Map<String, TwinClassDependentFieldBundleDTOv1> bundleMap = new LinkedHashMap<>();

        for (TwinClassFieldRuleEntity rule : rules) {
            // Build DTO for the rule (descriptor of the change)
            TwinClassFieldRuleDTOv1 ruleDto = ruleMapper.convert(rule, mapperContext);

            String keyHash = buildRuleDescriptorHash(rule);
            TwinClassDependentFieldBundleDTOv1 bundle = bundleMap.computeIfAbsent(keyHash, k -> new TwinClassDependentFieldBundleDTOv1()
                    .setKey(ruleDto)
                    .setConditions(new ArrayList<>()));

            // Accumulate condition DTOs (avoid duplicates)
            if (CollectionUtils.isNotEmpty(rule.getConditions())) {
                for (TwinClassFieldConditionEntity condition : rule.getConditions()) {
                    TwinClassFieldConditionDTOv1 condDto = conditionMapper.convert(condition, mapperContext);
                    boolean exists = bundle.getConditions().stream().anyMatch(c -> Objects.equals(c.id, condDto.id));
                    if (!exists)
                        bundle.getConditions().add(condDto);
                }
            }
        }
        return new ArrayList<>(bundleMap.values());
    }

    @Override
    public String getObjectCacheId(TwinClassFieldRuleEntity src) {
        return null; // no caching on individual entity
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return false;
    }

    /**
     * Build a hash based on attributes that define the change applied to the dependent field.
     * If two rules have the same target element, param key, overwritten value & datalist – they
     * are considered the same change descriptor.
     */
    private String buildRuleDescriptorHash(TwinClassFieldRuleEntity rule) {
        return rule.getTargetElement() + "|" +
                Optional.ofNullable(rule.getTargetParamKey()).orElse("") + "|" +
                Optional.ofNullable(rule.getDependentOverwrittenValue()).orElse("") + "|" +
                Optional.ofNullable(rule.getDependentOverwrittenDatalistId()).map(UUID::toString).orElse("");
    }
}
