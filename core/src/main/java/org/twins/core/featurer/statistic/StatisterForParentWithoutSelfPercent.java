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
import org.twins.core.service.i18n.I18nService;

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
    private TwinFieldSimpleRepository twinFieldSimpleRepository;
    @Autowired
    private TwinRepository twinRepository;
    @Autowired
    private I18nService i18nService;

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

        UUID paramLabelI18nId = labelI18nId.extract(properties);
        String labelTranslation = paramLabelI18nId != null ? i18nService.translateToLocale(paramLabelI18nId) : null;

        Map<UUID, TwinStatisticProgressPercent> ret = new HashMap<>();
        for (var headTwin : parentTwinAndPercentMap.entrySet()) {
            UUID uuid = headTwin.getKey();
            TwinStatisticProgressPercent.Item item = createItem(
                    (int) (headTwin.getValue() * 100),
                    key.extract(properties),
                    labelTranslation,
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