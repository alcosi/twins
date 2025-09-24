package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldRuleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldConditionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldRuleMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassFieldRuleMode.class)
public class TwinClassFieldRuleRestDTOMapper extends RestSimpleDTOMapper<TwinClassFieldRuleEntity, TwinClassFieldRuleDTOv1> {

    @MapperModePointerBinding(modes = TwinClassFieldConditionMode.TwinRule2TwinClassFieldConditionMode.class)
    private final TwinClassFieldConditionRestDTOMapper twinClassFieldConditionRestDTOMapper;

    @Override
    public void map(TwinClassFieldRuleEntity src, TwinClassFieldRuleDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (src == null || dst == null)
            return;
        // map scalar fields
        dst
                .setId(src.getId())
                .setDependentTwinClassFieldId(src.getDependentTwinClassFieldId())
                .setTargetElement(src.getTargetElement())
                .setTargetParamKey(src.getTargetParamKey())
                .setDependentOverwrittenValue(src.getDependentOverwrittenValue())
                .setDependentOverwrittenDatalistId(src.getDependentOverwrittenDatalistId())
                .setRulePriority(src.getRulePriority());

        // map conditions if present
        if (src.getConditions() != null && !src.getConditions().isEmpty()) {
            java.util.List<org.twins.core.dao.twinclass.TwinClassFieldConditionEntity> sorted = src.getConditions().stream()
                    .sorted(java.util.Comparator.comparing(org.twins.core.dao.twinclass.TwinClassFieldConditionEntity::getConditionOrder, java.util.Comparator.nullsLast(Integer::compareTo)))
                    .toList();
            dst.setConditions(twinClassFieldConditionRestDTOMapper.convertCollectionPostpone(sorted, mapperContext));
        }
    }

    @Override
    public String getObjectCacheId(TwinClassFieldRuleEntity src) {
        return src.getId() != null ? src.getId().toString() : null;
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        // For now we don’t have dedicated mode, always show
        return false;
    }
}
