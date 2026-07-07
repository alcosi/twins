package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.EntityExportService;
import org.twins.core.service.twin.TwinStatusExportService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinflow.TwinflowExportService;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwinClassExportService extends EntityExportService<TwinClassEntity> {
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldExportService twinClassFieldExportService;
    private final TwinStatusExportService twinStatusExportService;
    private final TwinflowExportService twinflowExportService;
    private final TwinStatusService twinStatusService;
    private final TwinflowService twinflowService;

    @Override
    public String exportCollectionToSql(Collection<TwinClassEntity> twinClasses) throws ServiceException {
        return exportToSql(
                twinClasses.stream().map(TwinClassEntity::getId).collect(Collectors.toSet()),
                true, true, true);
    }

    public String exportToSql(UUID twinClassId, boolean includeFields, boolean includeStatuses, boolean includeTwinflow) throws ServiceException {
        return exportToSql(Collections.singleton(twinClassId), includeFields, includeStatuses, includeTwinflow);
    }

    public String exportToSql(Set<UUID> twinClassIds, boolean includeFields, boolean includeStatuses, boolean includeTwinflow) throws ServiceException {
        Kit<TwinClassEntity, UUID> twinClassesKit = twinClassService.findEntitiesSafe(twinClassIds);

        if (twinClassesKit.isEmpty()) {
            return "";
        }

        List<TwinClassEntity> twinClasses = twinClassesKit.getList();

        // Collect i18n IDs from twin classes
        Set<UUID> i18nIds = new HashSet<>();
        for (TwinClassEntity twinClass : twinClasses) {
            if (twinClass.getNameI18NId() != null) {
                i18nIds.add(twinClass.getNameI18NId());
            }
            if (twinClass.getDescriptionI18NId() != null) {
                i18nIds.add(twinClass.getDescriptionI18NId());
            }
        }

        var sqlParts = new StringList();

        // i18n for twin classes
        sqlParts.addNotBlank(i18nExportService.exportToSql(i18nIds));
        // twin classes
        sqlParts.addNotBlank(sqlBuilder.buildInserts(twinClasses));

        // fields
        if (includeFields) {
            var allFields = twinClassFieldService.findByTwinClassIdIn(twinClassIds);
            sqlParts.addNotBlank(twinClassFieldExportService.exportCollectionToSql(allFields));
        }

        // statuses
        if (includeStatuses) {
            var allStatuses = twinStatusService.findByTwinClassIdIn(twinClassIds);
            sqlParts.addNotBlank(twinStatusExportService.exportToSql(allStatuses));
        }

        // twinflow
        if (includeTwinflow) {
            var twinflows = twinflowService.findByTwinClassIdIn(twinClassIds);
            sqlParts.addNotBlank(twinflowExportService.exportToSql(twinflows));
        }

        return String.join("\n", sqlParts);
    }
}
