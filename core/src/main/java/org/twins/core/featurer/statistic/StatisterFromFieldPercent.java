package org.twins.core.featurer.statistic;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twin.TwinFieldValueProjection;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsI18nId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_3803,
        name = "Statistic for child twins",
        description = "Statistic for child twins")
@RequiredArgsConstructor
public class StatisterFromFieldPercent extends Statister<TwinStatisticProgressPercent> {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");
    @FeaturerParam(name = "Key", description = "", order = 2)
    public static final FeaturerParamString key = new FeaturerParamString("key");
    @FeaturerParam(name = "Label i18n id", description = "", order = 3)
    public static final FeaturerParamUUID labelI18nId = new FeaturerParamUUIDTwinsI18nId("labelI18nId");
    @FeaturerParam(name = "Color", description = "", order = 4)
    public static final FeaturerParamString color = new FeaturerParamString("colorHex");
    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public Map<UUID, TwinStatisticProgressPercent> getStatistic(Properties properties, Set<UUID> forTwinIdSet) {
        Kit<TwinFieldValueProjection, UUID> twinFieldSimplekit = new Kit<>(TwinFieldValueProjection::headTwinId);
        List<TwinFieldValueProjection> forHeadTwinValues = twinFieldSimpleRepository.valueByTwinId(forTwinIdSet, twinClassFieldId.extract(properties));
        twinFieldSimplekit.addAll(forHeadTwinValues);


        Map<UUID, TwinStatisticProgressPercent> ret = new HashMap<>();
        for (UUID twinId : forTwinIdSet) {
            TwinFieldValueProjection twin = twinFieldSimplekit.get(twinId);
            TwinStatisticProgressPercent.Item item = createItem(
                    (int) (twin.value() * 100),
                    key.extract(properties),
                    labelI18nId.extract(properties),
                    color.extract(properties)
            );
            ret.put(twinId, new TwinStatisticProgressPercent()
                    .setItems(List.of(item)));
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