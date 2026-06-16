package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.service.EntityExportService;
import org.twins.core.service.factory.FactoryExportService;
import org.twins.core.service.permission.PermissionExportService;
import org.twins.core.service.twin.TwinStatusExportService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransitionExportService extends EntityExportService<TwinflowTransitionEntity> {
    private final TwinflowTransitionService twinflowTransitionService;
    private final TwinStatusExportService twinStatusExportService;
    private final FactoryExportService factoryExportService;
    private final PermissionExportService permissionExportService;

    @Override
    public String exportCollectionToSql(Collection<TwinflowTransitionEntity> transitions) throws ServiceException {
        return exportToSql(transitions, true, true, true, true, true);
    }

    public String exportToSql(Set<UUID> transitionIds,
                              boolean includeFactory,
                              boolean includeStatuses,
                              boolean includePermission,
                              boolean includeTriggers,
                              boolean includeValidatorRules) throws ServiceException {
        var transitions = twinflowTransitionService.findEntitiesSafe(transitionIds).getList();
        return exportToSql(transitions, includeFactory, includeStatuses, includePermission, includeTriggers, includeValidatorRules);
    }

    public String exportToSql(Collection<TwinflowTransitionEntity> transitions,
                              boolean includeFactory,
                              boolean includeStatuses,
                              boolean includePermission,
                              boolean includeTriggers,
                              boolean includeValidatorRules) throws ServiceException {
        if (CollectionUtils.isEmpty(transitions)) return "";

        var sqlParts = new StringList();

        // I18n for transitions (name, description)
        Set<UUID> transitionI18nIds = i18nService.collectI18nIds(transitions,
                TwinflowTransitionEntity::getNameI18NId,
                TwinflowTransitionEntity::getDescriptionI18NId);
        i18nExportService.addExportSafe(transitionI18nIds, sqlParts);

        // Statuses (src + dst) — exported before the transition due to FK order
        if (includeStatuses) {
            Set<TwinStatusEntity> statuses = new LinkedHashSet<>();
            for (TwinflowTransitionEntity transition : transitions) {
                if (transition.getSrcTwinStatus() != null) statuses.add(transition.getSrcTwinStatus());
                if (transition.getDstTwinStatus() != null) statuses.add(transition.getDstTwinStatus());
            }
            sqlParts.addNotBlank(twinStatusExportService.exportToSql(statuses));
        }

        // Permission — exported before the transition due to FK order
        if (includePermission) {
            Set<UUID> permissionIds = CollectionUtils.collect(transitions, TwinflowTransitionEntity::getPermissionId);
            sqlParts.addNotBlank(permissionExportService.exportToSql(permissionIds));
        }

        // Factories (inbuilt + drafting) — exported before the transition due to FK order
        if (includeFactory) {
            Set<UUID> factoryIds = new LinkedHashSet<>();
            factoryIds.addAll(CollectionUtils.collect(transitions, TwinflowTransitionEntity::getInbuiltTwinFactoryId));
            factoryIds.addAll(CollectionUtils.collect(transitions, TwinflowTransitionEntity::getDraftingTwinFactoryId));
            sqlParts.addNotBlank(factoryExportService.exportToSql(factoryIds, true, true, true, true, true, true));
        }

        // Transitions themselves
        sqlParts.addNotBlank(sqlBuilder.buildInserts(transitions));

        // Triggers — children of transition
        if (includeTriggers) {
            twinflowTransitionService.loadTriggers(transitions);
            exportChildrenKit(includeTriggers, transitions,
                    TwinflowTransitionEntity::getTriggersKit,
                    list -> sqlBuilder.buildInserts(list),
                    sqlParts);
        }

        // Validator rules — children of transition
        if (includeValidatorRules) {
            twinflowTransitionService.loadValidators(transitions);
            exportChildrenKit(includeValidatorRules, transitions,
                    TwinflowTransitionEntity::getValidatorRulesKit,
                    list -> sqlBuilder.buildInserts(list),
                    sqlParts);
        }

        return String.join("\n", sqlParts);
    }
}
