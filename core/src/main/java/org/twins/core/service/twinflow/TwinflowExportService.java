package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinflowExportService {
    private final TwinflowService twinflowService;
    private final SqlBuilder sqlBuilder;

    public void appendTwinflowSql(StringBuilder sql, Map<UUID, TwinflowEntity> twinflowByClass) {
        for (TwinflowEntity twinflow : twinflowByClass.values()) {
            String twinflowSql = sqlBuilder.buildInsert(twinflow);
            if (!twinflowSql.isEmpty()) {
                if (!sql.isEmpty()) sql.append("\n");
                sql.append(twinflowSql);
            }

            List<TwinflowSchemaMapEntity> schemaMaps = twinflowService.findTwinflowSchemaMapByTwinflowId(twinflow.getId());
            if (!schemaMaps.isEmpty()) {
                String schemaMapsSql = sqlBuilder.buildInserts(schemaMaps);
                if (!schemaMapsSql.isEmpty()) {
                    if (!sql.isEmpty()) sql.append("\n");
                    sql.append(schemaMapsSql);
                }
            }
        }
    }
}
