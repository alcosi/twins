package org.twins.core.featurer.statistic;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_3803,
        name = "Statistic for child twins",
        description = "Statistic for child twins")
@RequiredArgsConstructor
public class StatisterFromFieldPercent extends Statister<TwinStatisticProgressPercent> {
    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public Map<UUID, TwinStatisticProgressPercent> getStatistic(Set<UUID> forTwinIdSet, HashMap<String, String> params) throws ServiceException {
        UUID twinClassFieldId = UUID.fromString(params.get("twinClassFieldId"));
        List<TwinFieldSimpleEntity> simpleFields = twinFieldSimpleRepository.findByTwinIdInAndTwinClassFieldIdIn(forTwinIdSet, List.of(twinClassFieldId));
        Kit<TwinFieldSimpleEntity, UUID> twinFieldSimplekit = new Kit<>(simpleFields, TwinFieldSimpleEntity::getTwinId);

        Map<UUID, TwinStatisticProgressPercent> ret = new HashMap<>();
        for (UUID twinId : forTwinIdSet) {
            int percent;
            TwinFieldSimpleEntity entity = twinFieldSimplekit.get(twinId);
            if (entity != null) {
                try {
                    percent = (int) (Double.parseDouble(entity.getValue()) * 100);
                } catch (NumberFormatException e) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for percent compare with field: [" + entity.getTwinClassFieldId().toString() + "]");
                }
            } else {
                percent = 0;
            }
            TwinStatisticProgressPercent.Item item = createItem(
                    percent,
                    params.get(TwinStatisticProgressPercent.Item.Fields.key),
                    params.get(TwinStatisticProgressPercent.Item.Fields.label),
                    params.get(TwinStatisticProgressPercent.Item.Fields.colorHex)
            );
            ret.put(twinId, new TwinStatisticProgressPercent()
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