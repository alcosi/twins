package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.sql.SqlBuilder;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;
import org.twins.core.service.EntityExportService;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwinflowExportService {
    private final TwinflowService twinflowService;
    private final SqlBuilder sqlBuilder;
    private final I18nService i18nService;
    private final I18nExportService i18nExportService;

    public String exportToSql(Collection<TwinflowEntity> twinflows) throws ServiceException {
        if (twinflows.isEmpty()) {
            return "";
        }

        // Sort twinflows by id once so the whole export is deterministic (diff-able).
        List<TwinflowEntity> sortedTwinflows = new ArrayList<>(twinflows);
        sortedTwinflows.sort(Comparator.comparing(
                TwinflowEntity::getId, Comparator.nullsFirst(Comparator.naturalOrder())));

        Kit<TwinflowSchemaMapEntity, UUID> allSchemaMaps = new Kit<>(
                twinflowService.findTwinflowSchemaMapByTwinflowIdIn(
                        sortedTwinflows.stream().map(TwinflowEntity::getId).collect(Collectors.toSet())),
                TwinflowSchemaMapEntity::getId);

        Set<UUID> i18nIds = i18nService.collectI18nIds(sortedTwinflows,
                TwinflowEntity::getNameI18NId,
                TwinflowEntity::getDescriptionI18NId);

        var sqlParts = new StringList();

        if (!i18nIds.isEmpty()) {
            sqlParts.addNotBlank(i18nExportService.exportToSql(i18nIds));
        }

        StringBuilder result = new StringBuilder();
        for (TwinflowEntity twinflow : sortedTwinflows) {
            String twinflowSql = sqlBuilder.buildInsert(twinflow);
            if (!twinflowSql.isEmpty()) {
                if (!result.isEmpty()) result.append("\n");
                result.append(twinflowSql);
            }

            List<TwinflowSchemaMapEntity> schemaMaps = allSchemaMaps.getList().stream()
                    .filter(sm -> twinflow.getId().equals(sm.getTwinflowId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(schemaMaps)) {
                String schemaMapsSql = EntityExportService.buildInsertsSorted(
                        sqlBuilder, schemaMaps, TwinflowSchemaMapEntity::getId);
                if (!schemaMapsSql.isEmpty()) {
                    if (!result.isEmpty()) result.append("\n");
                    result.append(schemaMapsSql);
                }
            }
        }

        sqlParts.addNotBlank(result.toString());

        return String.join("\n", sqlParts);
    }
}
