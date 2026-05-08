package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.sql.SqlBuilder;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinStatusExportService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinflow.TwinflowExportService;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassExportService {
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldExportService twinClassFieldExportService;
    private final TwinStatusExportService twinStatusExportService;
    private final TwinStatusService twinStatusService;
    private final TwinflowExportService twinflowExportService;
    private final TwinflowService twinflowService;
    private final I18nService i18nService;
    private final I18nExportService i18nExportService;
    private final SqlBuilder sqlBuilder;

    public String exportToSql(UUID twinClassId, boolean includeFields, boolean includeStatuses, boolean includeTwinflow) throws ServiceException {
        return exportToSql(Collections.singleton(twinClassId), includeFields, includeStatuses, includeTwinflow);
    }

    public String exportToSql(Set<UUID> twinClassIds, boolean includeFields, boolean includeStatuses, boolean includeTwinflow) throws ServiceException {
        Kit<TwinClassEntity, UUID> twinClassesKit = twinClassService.findEntitiesSafe(twinClassIds);
        List<TwinClassEntity> twinClasses = twinClassesKit.getList();

        if (twinClasses.isEmpty()) {
            return "";
        }

        Map<UUID, List<TwinClassFieldEntity>> fieldsByClass = new HashMap<>();
        Map<UUID, List<TwinStatusEntity>> statusesByClass = new HashMap<>();
        Map<UUID, TwinflowEntity> twinflowByClass = new HashMap<>();

        if (includeFields) {
            twinClassFieldService.loadTwinClassFields(twinClasses);
            for (TwinClassEntity twinClass : twinClasses) {
                if (twinClass.getTwinClassFieldKit() != null) {
                    List<TwinClassFieldEntity> ownFields = CollectionUtils.filterByItemId(twinClass.getTwinClassFieldKit(), twinClass.getId(), TwinClassFieldEntity::getTwinClassId);
                    if (!ownFields.isEmpty()) {
                        fieldsByClass.put(twinClass.getId(), ownFields);
                    }
                }
            }
        }

        if (includeStatuses) {
            twinStatusService.loadStatusesForTwinClasses(twinClasses);
            for (TwinClassEntity twinClass : twinClasses) {
                if (twinClass.getTwinStatusKit() != null) {
                    List<TwinStatusEntity> ownStatuses = CollectionUtils.filterByItemId(twinClass.getTwinStatusKit(), twinClass.getId(), TwinStatusEntity::getTwinClassId);
                    if (!ownStatuses.isEmpty()) {
                        statusesByClass.put(twinClass.getId(), ownStatuses);
                    }
                }
            }
        }

        if (includeTwinflow) {
            List<UUID> classIds = twinClasses.stream().map(TwinClassEntity::getId).toList();
            List<TwinflowEntity> twinflows = twinflowService.findByTwinClassIdIn(classIds);
            for (TwinflowEntity twinflow : twinflows) {
                twinflowByClass.put(twinflow.getTwinClassId(), twinflow);
            }
        }

        Set<UUID> i18nIds = collectI18nIds(twinClasses, fieldsByClass, statusesByClass, twinflowByClass);

        StringBuilder result = new StringBuilder();
        i18nExportService.appendI18nSql(result, i18nIds);

        String twinClassesSql = sqlBuilder.buildInserts(twinClasses);
        if (!twinClassesSql.isEmpty()) {
            result.append(twinClassesSql);
        }

        if (!statusesByClass.isEmpty()) {
            twinStatusExportService.appendStatusesSql(result, statusesByClass);
        }

        if (!twinflowByClass.isEmpty()) {
            twinflowExportService.appendTwinflowSql(result, twinflowByClass);
        }

        if (!fieldsByClass.isEmpty()) {
            twinClassFieldExportService.appendFieldsSql(result, fieldsByClass);
        }

        return result.toString();
    }

    private Set<UUID> collectI18nIds(Collection<TwinClassEntity> twinClasses,
                                      Map<UUID, List<TwinClassFieldEntity>> fieldsByClass,
                                      Map<UUID, List<TwinStatusEntity>> statusesByClass,
                                      Map<UUID, TwinflowEntity> twinflowByClass) {
        Set<UUID> i18nIds = new HashSet<>();

        for (TwinClassEntity twinClass : twinClasses) {
            if (twinClass.getNameI18NId() != null) {
                i18nIds.add(twinClass.getNameI18NId());
            }
            if (twinClass.getDescriptionI18NId() != null) {
                i18nIds.add(twinClass.getDescriptionI18NId());
            }
        }

        for (List<TwinClassFieldEntity> fields : fieldsByClass.values()) {
            i18nIds.addAll(i18nService.collectI18nIds(fields,
                    TwinClassFieldEntity::getNameI18nId,
                    TwinClassFieldEntity::getDescriptionI18nId));
        }

        for (List<TwinStatusEntity> statuses : statusesByClass.values()) {
            i18nIds.addAll(i18nService.collectI18nIds(statuses,
                    TwinStatusEntity::getNameI18nId,
                    TwinStatusEntity::getDescriptionI18nId));
        }

        for (TwinflowEntity twinflow : twinflowByClass.values()) {
            if (twinflow.getNameI18NId() != null) {
                i18nIds.add(twinflow.getNameI18NId());
            }
            if (twinflow.getDescriptionI18NId() != null) {
                i18nIds.add(twinflow.getDescriptionI18NId());
            }
        }

        return i18nIds;
    }
}
