package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.service.EntityExportService;
import org.twins.core.service.twin.TwinStatusExportService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinflow.TwinflowExportService;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TwinClassExportService extends EntityExportService {
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldExportService twinClassFieldExportService;
    private final TwinStatusExportService twinStatusExportService;
    private final TwinflowExportService twinflowExportService;
    private final TwinStatusService twinStatusService;
    private final TwinflowService twinflowService;

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
        if (!i18nIds.isEmpty()) {
            sqlParts.addNotBlank(i18nExportService.exportToSql(i18nIds));
        }

        // twin classes
        sqlParts.addNotBlank(sqlBuilder.buildInserts(twinClasses));

        // fields
        if (includeFields) {
            List<TwinClassFieldEntity> allFields = twinClassFieldService.findByTwinClassIdIn(twinClassIds);
            if (!allFields.isEmpty()) {
                sqlParts.addNotBlank(twinClassFieldExportService.exportToSql(allFields));
            }
        }

        // statuses
        if (includeStatuses) {
            List<TwinStatusEntity> allStatuses = twinStatusService.findByTwinClassIdIn(twinClassIds);
            if (!allStatuses.isEmpty()) {
                sqlParts.addNotBlank(twinStatusExportService.exportToSql(allStatuses));
            }
        }

        // twinflow
        if (includeTwinflow) {
            List<TwinflowEntity> twinflows = twinflowService.findByTwinClassIdIn(twinClassIds);
            if (!twinflows.isEmpty()) {
                sqlParts.addNotBlank(twinflowExportService.exportToSql(twinflows));
            }
        }

        return String.join("\n", sqlParts);
    }
}
