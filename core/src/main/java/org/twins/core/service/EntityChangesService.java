package org.twins.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.ChangesHelper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.EntitiesChangesCollector;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntityChangesService {
    final TwinFieldRepository twinFieldRepository;
    final TwinFieldDataListRepository twinFieldDataListRepository;
    final TwinLinkRepository twinLinkRepository;
    final EntitySmartService entitySmartService;

    public void saveEntities(EntitiesChangesCollector entitiesChangesCollector) {
        if (!entitiesChangesCollector.hasChanges())
            return;
        saveEntities(entitiesChangesCollector, TwinFieldEntity.class, twinFieldRepository);
        saveEntities(entitiesChangesCollector, TwinFieldDataListEntity.class, twinFieldDataListRepository);
        saveEntities(entitiesChangesCollector, TwinLinkEntity.class, twinLinkRepository);
        if (entitiesChangesCollector.getSaveEntityMap().size() > 0)
            for (Map.Entry<Class, Map<Object, ChangesHelper>> classChanges : entitiesChangesCollector.getSaveEntityMap().entrySet()) {
                log.warn("Unsupported entity class[" + classChanges.getKey().getSimpleName() + "] for saving");
            }
        deleteEntities(entitiesChangesCollector, TwinLinkEntity.class, twinLinkRepository);
        deleteEntities(entitiesChangesCollector, TwinFieldDataListEntity.class, twinFieldDataListRepository);
        deleteEntities(entitiesChangesCollector, TwinFieldEntity.class, twinFieldRepository);
        if (entitiesChangesCollector.getDeleteEntityIdMap().size() > 0)
            for (Map.Entry<Class, List<UUID>> classChanges : entitiesChangesCollector.getDeleteEntityIdMap().entrySet()) {
                log.warn("Unsupported entity class[" + classChanges.getKey().getSimpleName() + "] for deletion");
            }
    }

    private <T> void saveEntities(EntitiesChangesCollector entitiesChangesCollector, Class<T> entityClass, CrudRepository<T, UUID> repository) {
        Map<Object, ChangesHelper> entities = entitiesChangesCollector.getSaveEntityMap().get(entityClass);
        if (entities != null) {
            entitySmartService.saveAllAndLogChanges((Map) entities, repository);
            entitiesChangesCollector.getSaveEntityMap().remove(entityClass);
        }
    }

    private <T> void deleteEntities(EntitiesChangesCollector entitiesChangesCollector, Class<T> entityClass, CrudRepository<T, UUID> repository) {
        List<UUID> entitiesId = entitiesChangesCollector.getDeleteEntityIdMap().get(entityClass);
        if (entitiesId != null) {
            entitySmartService.deleteAllAndLog(entitiesId, repository);
            entitiesChangesCollector.getDeleteEntityIdMap().remove(entityClass);
        }
    }
}
