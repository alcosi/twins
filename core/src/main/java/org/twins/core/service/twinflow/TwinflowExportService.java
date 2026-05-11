package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.sql.SqlBuilder;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;
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

        Kit<TwinflowSchemaMapEntity, UUID> allSchemaMaps = new Kit<>(
                twinflowService.findTwinflowSchemaMapByTwinflowIdIn(
                        twinflows.stream().map(TwinflowEntity::getId).collect(Collectors.toSet())),
                TwinflowSchemaMapEntity::getId);

        Set<UUID> i18nIds = i18nService.collectI18nIds(twinflows,
                TwinflowEntity::getNameI18NId,
                TwinflowEntity::getDescriptionI18NId);

        List<String> sqlParts = new ArrayList<>();

        if (!i18nIds.isEmpty()) {
            String i18nSql = i18nExportService.exportToSql(i18nIds);
            if (!i18nSql.isEmpty()) {
                sqlParts.add(i18nSql);
            }
        }

        StringBuilder result = new StringBuilder();
        for (TwinflowEntity twinflow : twinflows) {
            String twinflowSql = sqlBuilder.buildInsert(twinflow);
            if (!twinflowSql.isEmpty()) {
                if (!result.isEmpty()) result.append("\n");
                result.append(twinflowSql);
            }

            List<TwinflowSchemaMapEntity> schemaMaps = allSchemaMaps.getList().stream()
                    .filter(sm -> twinflow.getId().equals(sm.getTwinflowId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(schemaMaps)) {
                String schemaMapsSql = sqlBuilder.buildInserts(schemaMaps);
                if (!schemaMapsSql.isEmpty()) {
                    if (!result.isEmpty()) result.append("\n");
                    result.append(schemaMapsSql);
                }
            }
        }

        if (!result.isEmpty()) {
            sqlParts.add(result.toString());
        }

        return String.join("\n", sqlParts);
    }
}
