package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwinStatusExportService {
    private final SqlBuilder sqlBuilder;

    public void appendStatusesSql(StringBuilder sql, Map<UUID, List<TwinStatusEntity>> statusesByClass) {
        for (List<TwinStatusEntity> statuses : statusesByClass.values()) {
            String statusesSql = sqlBuilder.buildInserts(statuses);
            if (!statusesSql.isEmpty()) {
                if (!sql.isEmpty()) sql.append("\n");
                sql.append(statusesSql);
            }
        }
    }
}
