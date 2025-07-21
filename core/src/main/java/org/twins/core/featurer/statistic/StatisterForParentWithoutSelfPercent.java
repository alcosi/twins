package org.twins.core.featurer.statistic;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinSearchService;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_3801,
        name = "Statistic for parent without self percent",
        description = "Statistic for parent without self percent")
@RequiredArgsConstructor
public class StatisterForParentWithoutSelfPercent extends Statister<TwinStatisticProgressPercent> {
    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;
    @Autowired
    private TwinSearchService twinSearchService;

    @Override
    public Map<UUID, TwinStatisticProgressPercent> getStatistic(Set<UUID> forTwinIdSet, HashMap<String, String> params) throws ServiceException {
        //todo need check only child and grandchild
//        UUID twinClassId = UUID.fromString(params.get("twinClassId"));
//        UUID twinClassFieldId = UUID.fromString(params.get("twinClassFieldId"));
//
//        // init main collection
//        Map<UUID, List<TwinEntity>> percentForHeadTwinIds = new HashMap<>();
//        forTwinIdSet.forEach(twinId -> {percentForHeadTwinIds.put(twinId, null);});
//
//        BasicSearch basicSearch = new BasicSearch();
//        basicSearch
//                .setTwinClassIdList(Set.of(twinClassId))
//                .setHeadTwinIdList(forTwinIdSet);
//        // search all child twins
//        Kit<TwinEntity, UUID> childTwinKit = new Kit<>(twinSearchService.findTwins(basicSearch), TwinEntity::getId);
//        // grouping child twins by head
//        KitGrouped<TwinEntity, UUID, UUID> groupedKitByHead = new KitGrouped<>(childTwinKit, TwinEntity::getId, TwinEntity::getHeadTwinId);
//
//        for (var entry : percentForHeadTwinIds.entrySet()) {
//            entry.setValue(groupedKitByHead.getGrouped(entry.getKey()));
//        }
//
//        // collect twin ids for load field values
//        List<UUID> needLoad = new ArrayList<>();
//        for (var groupedChildTwinsByHead : percentForHeadTwinIds.entrySet()) {
//            if (CollectionUtils.isNotEmpty(groupedChildTwinsByHead.getValue())) {
//                groupedChildTwinsByHead.getValue().forEach(t -> {needLoad.add(t.getId());});
//            }
//        }
//
//        // load and grouping values by twin id
//        Kit<TwinFieldSimpleEntity, UUID> simpleFieldKit = new Kit<>(TwinFieldSimpleEntity::getTwinId);
//        simpleFieldKit.addAll(twinFieldSimpleRepository.findByTwinIdInAndTwinClassFieldIdIn(needLoad, List.of(twinClassFieldId)));
//
//        Map<UUID, TwinStatisticProgressPercent> ret = new HashMap<>();
//        for (var groupedChildTwinsByHead : percentForHeadTwinIds.entrySet()) {
//            UUID key = groupedChildTwinsByHead.getKey();
//            int percent;
//            if (groupedChildTwinsByHead.getValue().isEmpty()) {
//                percent = 0;
//            } else {
//                int countTwins = 0;
//                int sumPercent = 0;
//                for (TwinEntity twin : groupedChildTwinsByHead.getValue()) {
//                    countTwins++;
//                    sumPercent += parsePercentOrZero(simpleFieldKit.get(twin.getId()));
//                }
//                percent = sumPercent / countTwins; //rounding down (45.99 -> 45)
//            }
//            TwinStatisticProgressPercent.Item item = createItem(
//                    percent,
//                    params.get(TwinStatisticProgressPercent.Item.Fields.key),
//                    params.get(TwinStatisticProgressPercent.Item.Fields.label),
//                    params.get(TwinStatisticProgressPercent.Item.Fields.colorHex)
//            );
//            ret.put(key, new TwinStatisticProgressPercent()
//                    .setItems(List.of(item)));
//        }
//        return ret;
//    }
//
//    private TwinStatisticProgressPercent.Item createItem(Integer percent, String key, String label, String colorHex) {
//        return new TwinStatisticProgressPercent.Item()
//                .setKey(key)
//                .setLabel(label)
//                .setPercent(percent)
//                .setColorHex(colorHex);
//    }
//
//    private Integer parsePercentOrZero(TwinFieldSimpleEntity fieldSimple) throws ServiceException {
//        int percent;
//        if (fieldSimple == null) {
//            percent = 0;
//        } else {
//            try {
//                percent = (int) (Double.parseDouble(fieldSimple.getValue()) * 100);
//            } catch (NumberFormatException e) {
//                throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Incorrect value for percent compare with field: [" + fieldSimple.getTwinClassFieldId().toString() + "]");
//            }
//        }
//        return percent;
        return null;
    }
}