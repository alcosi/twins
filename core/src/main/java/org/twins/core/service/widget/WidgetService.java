package org.twins.core.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.AccessRule;
import org.twins.core.dao.widget.WidgetAccessEntity;
import org.twins.core.dao.widget.WidgetAccessRepository;
import org.twins.core.dao.widget.WidgetEntity;
import org.twins.core.dao.widget.WidgetRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetService {
    final WidgetRepository widgetRepository;
    final WidgetAccessRepository widgetAccessRepository;

    public List<WidgetEntity> findWidgets(UUID twinClassId) {
        List<WidgetEntity> allWidgets = widgetRepository.findAll();
        List<WidgetAccessEntity> widgetAccessList = widgetAccessRepository.findByTwinClassIdOrTwinClassIdIsNull(twinClassId);
        Map<UUID, WidgetEntity> widgetsAllowed = widgetAccessList.stream().filter(a -> a.getAccessRule() == AccessRule.Allow).collect(Collectors.toMap(WidgetAccessEntity::getWidgetId, WidgetAccessEntity::getWidget));
        Map<UUID, WidgetEntity> widgetsDeny = widgetAccessList.stream().filter(a -> a.getAccessRule() == AccessRule.Deny).collect(Collectors.toMap(WidgetAccessEntity::getWidgetId, WidgetAccessEntity::getWidget));

        ListIterator<WidgetEntity> iter = allWidgets.listIterator();
        WidgetEntity widgetEntity;
        while (iter.hasNext()) {
            widgetEntity = iter.next();
            switch (widgetEntity.accessOrder()) {
                case AllowDeny:
                    if (widgetsAllowed.containsKey(widgetEntity.id()) && !widgetsDeny.containsKey(widgetEntity.id()))
                        continue;
                    else
                        iter.remove();
                case DenyAllow:
                    if (widgetsDeny.containsKey(widgetEntity.id()) && !widgetsAllowed.containsKey(widgetEntity.id()))
                        iter.remove();
            }
        }
        return allWidgets;

//        Map<UUID, WidgetEntity> widgetsAllowedForTwinClass = widgetAccessRepository
//                .findByTwinClassIdAndAccessRuleEquals(twinClassId, AccessRule.Allow).stream().collect(Collectors.toMap(WidgetAccessEntity::getWidgetId, WidgetAccessEntity::getWidget));
//        Map<UUID, WidgetEntity> widgetsAllowedForAll = widgetAccessRepository
//                .findByTwinClassIdIsNullAndAccessRule(AccessRule.Allow).stream().collect(Collectors.toMap(WidgetAccessEntity::getWidgetId, WidgetAccessEntity::getWidget));
//        Map<UUID, WidgetEntity> widgetsDenyForTwinClass = widgetAccessRepository
//                .findByTwinClassIdAndAccessRuleEquals(twinClassId, AccessRule.Deny).stream().collect(Collectors.toMap(WidgetAccessEntity::getWidgetId, WidgetAccessEntity::getWidget));
//        Map<UUID, WidgetEntity> widgetsDenyForAll = widgetAccessRepository
//                .findByTwinClassIdIsNullAndAccessRule(AccessRule.Deny).stream().collect(Collectors.toMap(WidgetAccessEntity::getWidgetId, WidgetAccessEntity::getWidget));
//        ListIterator<WidgetEntity> iter = allWidgets.listIterator();
//        WidgetEntity widgetEntity;
//        while (iter.hasNext()) {
//            widgetEntity = iter.next();
//            switch (widgetEntity.accessOrder()) {
//                case AllowDeny:
//                    if ((widgetsAllowedForTwinClass.containsKey(widgetEntity.id()) || widgetsAllowedForAll.containsKey(widgetEntity.id()))
//                            && !widgetsDenyForTwinClass.containsKey(widgetEntity.id())
//                            && !widgetsDenyForAll.containsKey(widgetEntity.id()))
//                        continue;
//                    else
//                        iter.remove();
//                case DenyAllow:
//                    if ((widgetsDenyForTwinClass.containsKey(widgetEntity.id()) || widgetsDenyForAll.containsKey(widgetEntity.id()))
//                            && !widgetsAllowedForTwinClass.containsKey(widgetEntity.id())
//                            && !widgetsAllowedForAll.containsKey(widgetEntity.id()))
//                        iter.remove();
//            }
//        }
//        return allWidgets;
    }
}

