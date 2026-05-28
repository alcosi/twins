package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.sql.SqlBuilder;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.service.i18n.I18nExportService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactoryExportService {
    private final TwinFactoryService twinFactoryService;
    private final I18nExportService i18nExportService;
    private final SqlBuilder sqlBuilder;

    private final FactoryBranchExportService branchExportService;
    private final FactoryMultiplierExportService multiplierExportService;
    private final FactoryPipelineExportService pipelineExportService;
    private final FactoryEraserExportService eraserExportService;
    private final FactoryTriggerExportService triggerExportService;

    private final FactoryBranchService factoryBranchService;
    private final FactoryMultiplierService factoryMultiplierService;
    private final FactoryPipelineService factoryPipelineService;
    private final FactoryEraserService factoryEraserService;
    private final FactoryTriggerService factoryTriggerService;

    public String exportToSql(UUID factoryId, boolean includeBranches, boolean includeMultipliers, boolean includePipelines, boolean includeErasers, boolean includeTriggers) throws ServiceException {
        return exportToSql(Collections.singleton(factoryId), includeBranches, includeMultipliers, includePipelines, includeErasers, includeTriggers);
    }

    public String exportToSql(Set<UUID> factoryIds, boolean includeBranches, boolean includeMultipliers, boolean includePipelines, boolean includeErasers, boolean includeTriggers) throws ServiceException {
        var factoriesKit = twinFactoryService.findEntitiesSafe(factoryIds);

        if (factoriesKit.isEmpty()) {
            return "";
        }

        List<TwinFactoryEntity> factories = factoriesKit.getList();
        List<String> sqlParts = new ArrayList<>();

        // Collect I18n IDs from factories
        Set<UUID> i18nIds = new HashSet<>();
        for (TwinFactoryEntity factory : factories) {
            if (factory.getNameI18NId() != null) {
                i18nIds.add(factory.getNameI18NId());
            }
            if (factory.getDescriptionI18NId() != null) {
                i18nIds.add(factory.getDescriptionI18NId());
            }
        }

        // I18n for factories
        if (!i18nIds.isEmpty()) {
            String i18nSql = i18nExportService.exportToSql(i18nIds);
            if (!i18nSql.isEmpty()) {
                sqlParts.add(i18nSql);
            }
        }

        // Factories
        String factoriesSql = sqlBuilder.buildInserts(factories);
        if (!factoriesSql.isEmpty()) {
            sqlParts.add(factoriesSql);
        }

        // Load all factory elements using optimized load methods
        twinFactoryService.loadFactoryElements(factories);

        // Branches
        if (includeBranches) {
            List<TwinFactoryBranchEntity> branches = new ArrayList<>();
            for (TwinFactoryEntity factory : factories) {
                branches.addAll(factory.getTwinFactoryBranchKit().getList());
            }
            if (!branches.isEmpty()) {
                String branchesSql = branchExportService.exportToSql(branches);
                if (!branchesSql.isEmpty()) {
                    sqlParts.add(branchesSql);
                }
            }
        }

        // Multipliers
        if (includeMultipliers) {
            List<TwinFactoryMultiplierEntity> multipliers = new ArrayList<>();
            for (TwinFactoryEntity factory : factories) {
                multipliers.addAll(factory.getTwinFactoryMultiplierKit().getList());
            }
            if (!multipliers.isEmpty()) {
                String multipliersSql = multiplierExportService.exportToSql(multipliers);
                if (!multipliersSql.isEmpty()) {
                    sqlParts.add(multipliersSql);
                }
            }
        }

        // Pipelines
        if (includePipelines) {
            List<TwinFactoryPipelineEntity> pipelines = new ArrayList<>();
            for (TwinFactoryEntity factory : factories) {
                pipelines.addAll(factory.getTwinFactoryPipelineKit().getList());
            }
            if (!pipelines.isEmpty()) {
                String pipelinesSql = pipelineExportService.exportToSql(pipelines);
                if (!pipelinesSql.isEmpty()) {
                    sqlParts.add(pipelinesSql);
                }
            }
        }

        // Erasers
        if (includeErasers) {
            List<TwinFactoryEraserEntity> erasers = new ArrayList<>();
            for (TwinFactoryEntity factory : factories) {
                erasers.addAll(factory.getTwinFactoryEraserKit().getList());
            }
            if (!erasers.isEmpty()) {
                String erasersSql = eraserExportService.exportToSql(erasers);
                if (!erasersSql.isEmpty()) {
                    sqlParts.add(erasersSql);
                }
            }
        }

        // Triggers
        if (includeTriggers) {
            List<TwinFactoryTriggerEntity> triggers = new ArrayList<>();
            for (TwinFactoryEntity factory : factories) {
                triggers.addAll(factory.getTwinFactoryTriggerKit().getList());
            }
            if (!triggers.isEmpty()) {
                String triggersSql = triggerExportService.exportToSql(triggers);
                if (!triggersSql.isEmpty()) {
                    sqlParts.add(triggersSql);
                }
            }
        }

        return String.join("\n", sqlParts);
    }
}
