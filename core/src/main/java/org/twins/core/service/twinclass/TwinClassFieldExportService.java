package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinClassFieldExportService {
    private final SqlBuilder sqlBuilder;

    public void appendFieldsSql(StringBuilder sql, Map<UUID, List<TwinClassFieldEntity>> fieldsByClass) {
        for (List<TwinClassFieldEntity> fields : fieldsByClass.values()) {
            String fieldsSql = sqlBuilder.buildInserts(fields);
            if (!fieldsSql.isEmpty()) {
                if (!sql.isEmpty()) sql.append("\n");
                sql.append(fieldsSql);
            }
        }
    }
}
