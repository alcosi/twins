package org.twins.core.featurer.statistic;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsI18nId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.math.BigDecimal;
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
    @FeaturerParam(name = "Twin class ids", description = "", order = 6)
    public static final FeaturerParamUUIDSet ofChildTwinClassIds = new FeaturerParamUUIDSetTwinsClassId("ofChildTwinClassIds");
    @FeaturerParam(name = "Key", description = "", order = 3)
    public static final FeaturerParamString key = new FeaturerParamString("key");
    @FeaturerParam(name = "Label i18n id", description = "", order = 4)
    public static final FeaturerParamUUID labelI18nId = new FeaturerParamUUIDTwinsI18nId("labelI18nId");
    @FeaturerParam(name = "Color", description = "", order = 5)
    public static final FeaturerParamString colorHex = new FeaturerParamString("colorHex");
    @Autowired
    private TwinFieldDecimalRepository twinFieldDecimalRepository;
    @Autowired
    private TwinRepository twinRepository;

    @Override
    public Map<UUID, TwinStatisticProgressPercent> getStatistic(Properties properties, Set<UUID> forTwinIdSet) {
        List<TwinNoRelationsProjection> twinChildProjections = twinRepository.findByHeadTwinIdInAndTwinClassIdIn(forTwinIdSet, ofChildTwinClassIds.extract(properties), TwinNoRelationsProjection.class);

        List<UUID> allChildTwinIds = new ArrayList<>();
        Map<UUID, List<UUID>> groupedByParentMap = new HashMap<>();

        for (TwinNoRelationsProjection projection : twinChildProjections) {
            groupedByParentMap.computeIfAbsent(projection.headTwinId(), k -> new ArrayList<>()).add(projection.id());
            allChildTwinIds.add(projection.id());
        }

        Kit<TwinFieldHeadSumCountProjection, UUID> groupingByHead = new Kit<>(TwinFieldHeadSumCountProjection::headTwinId);
        groupingByHead.addAll(twinFieldDecimalRepository.sumAndCountByHeadTwinId( allChildTwinIds, grandChildTwinClassFieldId.extract(properties)));

        Map<UUID, Double> twinAndPercentMap = new HashMap<>();
        List<UUID> needLoad = new ArrayList<>();

        for (UUID headId : allChildTwinIds) {
            TwinFieldHeadSumCountProjection headSum = groupingByHead.get(headId);
            if (headSum == null) {
                needLoad.add(headId);
                continue;
            }

            BigDecimal sum = headSum.sum();
            long count = headSum.count();

            if (sum == null || count == 0) {
                twinAndPercentMap.put(headId, 0.0);
                continue;
            }

            double percent = sum.doubleValue() / count;
            twinAndPercentMap.put(headId, percent);
        }

        if (CollectionUtils.isNotEmpty(needLoad)) {
            List<TwinFieldValueProjection> forHeadTwinValues = twinFieldDecimalRepository.valueByTwinId(needLoad, childTwinClassFieldId.extract(properties));

            for (TwinFieldValueProjection valueProjection : forHeadTwinValues) {
                double value = valueProjection.value() != null ? valueProjection.value().doubleValue() : 0.0;
                twinAndPercentMap.put(valueProjection.headTwinId(), value);
            }
        }

        Map<UUID, Double> parentTwinAndPercentMap = new HashMap<>();

        for (var entry : groupedByParentMap.entrySet()) {
            double sum = 0.0;
            int count = 0;

            for (UUID childId : entry.getValue()) {
                Double value = twinAndPercentMap.get(childId);
                if (value != null) {
                    sum += value;
                    count++;
                }
            }

            parentTwinAndPercentMap.put(
                    entry.getKey(),
                    count == 0 ? 0.0 : sum / count
            );
        }

        Map<UUID, TwinStatisticProgressPercent> ret = new HashMap<>();

        for (var entry : parentTwinAndPercentMap.entrySet()) {
            TwinStatisticProgressPercent.Item item = createItem(
                    (int) (entry.getValue() * 100),
                    key.extract(properties),
                    labelI18nId.extract(properties),
                    colorHex.extract(properties)
            );

            ret.put(entry.getKey(), new TwinStatisticProgressPercent().setItems(List.of(item)));
        }

        return ret;
    }

    private TwinStatisticProgressPercent.Item createItem(Integer percent, String key, UUID labelI18nId, String colorHex) {
        return new TwinStatisticProgressPercent.Item()
                .setKey(key)
                .setLabelI18nId(labelI18nId)
                .setPercent(percent)
                .setColorHex(colorHex);
    }
}