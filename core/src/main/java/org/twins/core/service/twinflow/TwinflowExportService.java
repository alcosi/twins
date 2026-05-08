package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.sql.SqlBuilder;

import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwinflowExportService {
    private final TwinflowService twinflowService;
    private final SqlBuilder sqlBuilder;

    public void appendTwinflowSql(StringBuilder sql, Map<UUID, TwinflowEntity> twinflowByClass) {
        if (twinflowByClass.isEmpty()) {
            return;
        }

        Collection<UUID> twinflowIds = twinflowByClass.values().stream()
                .map(TwinflowEntity::getId)
                .toList();

        List<TwinflowSchemaMapEntity> allSchemaMaps = twinflowService.findTwinflowSchemaMapByTwinflowIdIn(twinflowIds);
        Map<UUID, List<TwinflowSchemaMapEntity>> schemaMapsByTwinflowId = allSchemaMaps.stream()
                .collect(Collectors.groupingBy(TwinflowSchemaMapEntity::getTwinflowId));

        for (TwinflowEntity twinflow : twinflowByClass.values()) {
            String twinflowSql = sqlBuilder.buildInsert(twinflow);
            if (!twinflowSql.isEmpty()) {
                if (!sql.isEmpty()) sql.append("\n");
                sql.append(twinflowSql);
            }

            List<TwinflowSchemaMapEntity> schemaMaps = schemaMapsByTwinflowId.get(twinflow.getId());
            if (CollectionUtils.isNotEmpty(schemaMaps)) {
                String schemaMapsSql = sqlBuilder.buildInserts(schemaMaps);
                if (!schemaMapsSql.isEmpty()) {
                    if (!sql.isEmpty()) sql.append("\n");
                    sql.append(schemaMapsSql);
                }
            }
        }
    }
}
