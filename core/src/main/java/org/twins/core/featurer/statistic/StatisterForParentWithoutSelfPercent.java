package org.twins.core.featurer.statistic;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_3801,
        name = "Statistic for parent without self percent",
        description = "Statistic for parent without self percent (child and grandchild)")
@RequiredArgsConstructor
public class StatisterForParentWithoutSelfPercent extends Statister<TwinStatisticProgressPercent> {
    @FeaturerParam(name = "Head twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID childTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("childTwinClassFieldId");
    @FeaturerParam(name = "Child twin class field id", description = "", order = 2)
    public static final FeaturerParamUUID grandChildTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("grandChildTwinClassFieldId");
    @FeaturerParam(name = "Key", description = "", order = 3)
    public static final FeaturerParamString key = new FeaturerParamString("key");
    @FeaturerParam(name = "Label", description = "", order = 4)
    public static final FeaturerParamString label = new FeaturerParamString("label");
    @FeaturerParam(name = "Color", description = "", order = 5)
    public static final FeaturerParamString colorHex = new FeaturerParamString("colorHex");
    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;
    @Autowired
    private TwinRepository twinRepository;

    @Override
    public Map<UUID, TwinStatisticProgressPercent> getStatistic(Properties properties, Set<UUID> forTwinIdSet) {
        List<TwinNoRelationsProjection> twinChildProjections = twinRepository.findByHeadTwinIdIn(forTwinIdSet, TwinNoRelationsProjection.class);
        List<UUID> allChildTwinIds = new ArrayList<>();
        Map<UUID, List<UUID>> groupedByParentMap = new HashMap<>();
        for (TwinNoRelationsProjection projection : twinChildProjections) {
            groupedByParentMap.computeIfAbsent(projection.getHeadTwinId(), k -> new ArrayList<>()).add(projection.getId());
            allChildTwinIds.add(projection.getId());
        }

        Kit<TwinFieldHeadSumCountProjection, UUID> groupingByHead = new Kit<>(TwinFieldHeadSumCountProjection::headTwinId);
        groupingByHead.addAll(twinFieldSimpleRepository.sumAndCountByHeadTwinId(allChildTwinIds, grandChildTwinClassFieldId.extract(properties)));

        Map<UUID, Double> twinAndPercentMap = new HashMap<>();
        List<UUID> needLoad = new ArrayList<>();
        for (UUID headId : allChildTwinIds) {
            TwinFieldHeadSumCountProjection headSum = groupingByHead.get(headId);
            if (headSum == null) {
                needLoad.add(headId);
            } else {
                twinAndPercentMap.put(headId, headSum.sum() / headSum.count());
            }
        }
        if (CollectionUtils.isNotEmpty(needLoad)) {
            List<TwinFieldValueProjection> forHeadTwinValues = twinFieldSimpleRepository.valueByTwinId(needLoad, childTwinClassFieldId.extract(properties));
            for (TwinFieldValueProjection forHeadTwinValue : forHeadTwinValues) {
                twinAndPercentMap.put(forHeadTwinValue.headTwinId(), forHeadTwinValue.value());
            }
        }

        Map<UUID, Double> parentTwinAndPercentMap = new HashMap<>();
        for (var entry : groupedByParentMap.entrySet()) {
            double sum = 0.0;
            int count = 0;
            for (UUID childId : entry.getValue()) {
                sum += twinAndPercentMap.get(childId);
                count++;
            }
            parentTwinAndPercentMap.put(entry.getKey(), sum / count);
        }

        Map<UUID, TwinStatisticProgressPercent> ret = new HashMap<>();
        for (var headTwin : parentTwinAndPercentMap.entrySet()) {
            UUID uuid = headTwin.getKey();
            TwinStatisticProgressPercent.Item item = createItem(
                    (int) (headTwin.getValue() * 100),
                    key.extract(properties),
                    label.extract(properties),
                    colorHex.extract(properties)
            );
            ret.put(uuid, new TwinStatisticProgressPercent()
                    .setItems(List.of(item)));
        }
        return ret;
    }

    private TwinStatisticProgressPercent.Item createItem(Integer percent, String key, String label, String colorHex) {
        return new TwinStatisticProgressPercent.Item()
                .setKey(key)
                .setLabel(label)
                .setPercent(percent)
                .setColorHex(colorHex);
    }
}