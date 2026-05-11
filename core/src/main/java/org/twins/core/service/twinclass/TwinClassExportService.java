package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
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
import org.twins.core.service.twin.TwinStatusExportService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinflow.TwinflowExportService;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwinClassExportService {
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinClassFieldExportService twinClassFieldExportService;
    private final TwinStatusExportService twinStatusExportService;
    private final TwinflowExportService twinflowExportService;
    private final TwinStatusService twinStatusService;
    private final TwinflowService twinflowService;
    private final I18nExportService i18nExportService;
    private final SqlBuilder sqlBuilder;

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

        List<String> sqlParts = new ArrayList<>();

        // i18n for twin classes
        if (!i18nIds.isEmpty()) {
            String i18nSql = i18nExportService.exportToSql(i18nIds);
            if (!i18nSql.isEmpty()) {
                sqlParts.add(i18nSql);
            }
        }

        // twin classes
        String twinClassesSql = sqlBuilder.buildInserts(twinClasses);
        if (!twinClassesSql.isEmpty()) {
            sqlParts.add(twinClassesSql);
        }

        // fields
        if (includeFields) {
            List<TwinClassFieldEntity> allFields = twinClassFieldService.findByTwinClassIdIn(twinClassIds);
            if (!allFields.isEmpty()) {
                String fieldsSql = twinClassFieldExportService.exportToSql(allFields);
                if (!fieldsSql.isEmpty()) {
                    sqlParts.add(fieldsSql);
                }
            }
        }

        // statuses
        if (includeStatuses) {
            List<TwinStatusEntity> allStatuses = twinStatusService.findByTwinClassIdIn(twinClassIds);
            if (!allStatuses.isEmpty()) {
                String statusesSql = twinStatusExportService.exportToSql(allStatuses);
                if (!statusesSql.isEmpty()) {
                    sqlParts.add(statusesSql);
                }
            }
        }

        // twinflow
        if (includeTwinflow) {
            List<TwinflowEntity> twinflows = twinflowService.findByTwinClassIdIn(twinClassIds);
            if (!twinflows.isEmpty()) {
                String twinflowSql = twinflowExportService.exportToSql(twinflows);
                if (!twinflowSql.isEmpty()) {
                    sqlParts.add(twinflowSql);
                }
            }
        }

        return String.join("\n", sqlParts);
    }
}
