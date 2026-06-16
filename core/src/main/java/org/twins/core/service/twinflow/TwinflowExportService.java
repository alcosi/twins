package org.twins.core.service.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.StringList;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.sql.SqlBuilder;
import org.cambium.common.util.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinflow.*;
import org.twins.core.service.factory.FactoryExportService;
import org.twins.core.service.i18n.I18nExportService;
import org.twins.core.service.i18n.I18nService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TwinflowExportService {
    private final TwinflowService twinflowService;
    private final TwinflowFactoryService twinflowFactoryService;
    private final TwinflowTransitionRepository twinflowTransitionRepository;
    @Lazy
    private final TransitionExportService transitionExportService;
    @Lazy
    private final FactoryExportService factoryExportService;
    private final SqlBuilder sqlBuilder;
    private final I18nService i18nService;
    private final I18nExportService i18nExportService;

    public String exportToSql(Collection<TwinflowEntity> twinflows) throws ServiceException {
        return exportToSql(twinflows, false, false);
    }

    public String exportToSql(Set<UUID> twinflowIds, boolean includeFactories, boolean includeTransitions) throws ServiceException {
        List<TwinflowEntity> twinflows = twinflowService.findEntitiesSafe(twinflowIds).getList();
        return exportToSql(twinflows, includeFactories, includeTransitions);
    }

    public String exportToSql(Collection<TwinflowEntity> twinflows, boolean includeFactories, boolean includeTransitions) throws ServiceException {
        if (twinflows.isEmpty()) {
            return "";
        }

        Set<UUID> twinflowIds = CollectionUtils.collect(twinflows, TwinflowEntity::getId);

        Kit<TwinflowSchemaMapEntity, UUID> allSchemaMaps = new Kit<>(
                twinflowService.findTwinflowSchemaMapByTwinflowIdIn(twinflowIds),
                TwinflowSchemaMapEntity::getId);

        Set<UUID> i18nIds = i18nService.collectI18nIds(twinflows,
                TwinflowEntity::getNameI18NId,
                TwinflowEntity::getDescriptionI18NId);

        var sqlParts = new StringList();

        i18nExportService.addExportSafe(i18nIds, sqlParts);

        // Twinflows + schema maps
        StringBuilder twinflowSql = new StringBuilder();
        for (TwinflowEntity twinflow : twinflows) {
            String insert = sqlBuilder.buildInsert(twinflow);
            if (!insert.isEmpty()) {
                if (!twinflowSql.isEmpty()) twinflowSql.append("\n");
                twinflowSql.append(insert);
            }

            List<TwinflowSchemaMapEntity> schemaMaps = allSchemaMaps.getList().stream()
                    .filter(sm -> twinflow.getId().equals(sm.getTwinflowId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(schemaMaps)) {
                String schemaMapsSql = sqlBuilder.buildInserts(schemaMaps);
                if (!schemaMapsSql.isEmpty()) {
                    if (!twinflowSql.isEmpty()) twinflowSql.append("\n");
                    twinflowSql.append(schemaMapsSql);
                }
            }
        }
        sqlParts.addNotBlank(twinflowSql.toString());

        // Twinflow factories (factory entities + link rows)
        if (includeFactories) {
            twinflowFactoryService.loadFactories(twinflows);
            List<TwinflowFactoryEntity> factoryLinks = twinflows.stream()
                    .map(TwinflowEntity::getFactoriesKit)
                    .filter(Objects::nonNull)
                    .flatMap(kit -> kit.getCollection().stream())
                    .toList();
            if (!factoryLinks.isEmpty()) {
                Set<UUID> factoryIds = CollectionUtils.collect(factoryLinks, TwinflowFactoryEntity::getTwinFactoryId);
                if (!factoryIds.isEmpty()) {
                    sqlParts.addNotBlank(factoryExportService.exportToSql(
                            factoryIds, true, true, true, true, true, true));
                }
                sqlParts.addNotBlank(sqlBuilder.buildInserts(factoryLinks));
            }
        }

        // Transitions (with all dependencies)
        if (includeTransitions) {
            List<TwinflowTransitionEntity> transitions = twinflowTransitionRepository.findByTwinflowIdIn(twinflowIds);
            if (CollectionUtils.isNotEmpty(transitions)) {
                Set<UUID> transitionIds = CollectionUtils.collect(transitions, TwinflowTransitionEntity::getId);
                sqlParts.addNotBlank(transitionExportService.exportToSql(
                        transitionIds, true, true, true, true, true));
            }
        }

        return String.join("\n", sqlParts);
    }
}
