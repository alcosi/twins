package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTagEntity;
import org.twins.core.dao.twin.TwinTagRepository;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.datalist.DataListService;

import java.util.*;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinTagService extends EntitySecureFindServiceImpl<TwinTagEntity> {
    final TwinTagRepository twinTagRepository;
    final TwinService twinService;
    final DataListService dataListService;
    final EntitySmartService entitySmartService;
    final I18nService i18nService;

    @Override
    public CrudRepository<TwinTagEntity, UUID> entityRepository() {
        return twinTagRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinTagEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinTagEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinId");
        if (entity.getTagDataListOptionId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinStatusId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwin() == null)
                    entity.setTwin(twinService.findEntitySafe(entity.getTwinId()));
                if (entity.getTagDataListOption() == null)
                    entity.setTagDataListOption(dataListService.findDataListOption(entity.getTagDataListOptionId()));
            default:
                if (!entity.getTwin().getTwinClass().getTagDataListId().equals(entity.getTagDataListOption().getDataListId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect twinTag dataListOptionId[" + entity.getTagDataListOptionId() + "]");
        }
        return true;
    }

    public List<TwinTagEntity> findByTwinId(UUID twinId) {
        return twinTagRepository.findByTwinId(twinId);
    }

    public List<DataListOptionEntity> findDataListOptionByTwinId(UUID twinId) {
        return twinTagRepository.findDataListOptionByTwinId(twinId);
    }

    public Kit<DataListOptionEntity> loadTags(TwinEntity twinEntity) {
        if (twinEntity.getTwinTagKit() != null)
            return twinEntity.getTwinTagKit();
        List<DataListOptionEntity> dataListOptionEntityList = findDataListOptionByTwinId(twinEntity.getId());
        // it's a side effect, but reading a method one would say it's just load / get based on return type too.
        if (dataListOptionEntityList != null)
            twinEntity.setTwinTagKit(new Kit<>(dataListOptionEntityList, DataListOptionEntity::getId));
        return twinEntity.getTwinTagKit();
    }

    // usage log

    public void loadTags(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getTwinTagKit() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.size() == 0)
            return;
        List<TwinTagEntity> twinTagEntityList = twinTagRepository.findByTwinIdIn(needLoad.keySet());
        if (CollectionUtils.isEmpty(twinTagEntityList))
            return;
        Map<UUID, List<DataListOptionEntity>> fieldsMap = new HashMap<>(); // key - twinId
        for (TwinTagEntity twinTagEntity : twinTagEntityList) { //grouping by twin
            fieldsMap.computeIfAbsent(twinTagEntity.getTwinId(), k -> new ArrayList<>());
            fieldsMap.get(twinTagEntity.getTwinId()).add(twinTagEntity.getTagDataListOption());
        }
        TwinEntity twinEntity;
        for (Map.Entry<UUID, List<DataListOptionEntity>> entry : fieldsMap.entrySet()) {
            twinEntity = needLoad.get(entry.getKey());
            twinEntity.setTwinTagKit(new Kit<>(entry.getValue(), DataListOptionEntity::getId));
        }
    }

    public void addTags(TwinEntity twinEntity, Set<String> newTags) throws ServiceException {
        if (CollectionUtils.isEmpty(newTags)) {
            return;
        }

        List<DataListOptionEntity> tagOptions = new ArrayList<>();

        List<TwinTagEntity> tagsToSave = newTags.stream()
                .filter(tag -> !twinTagRepository.existsByTagName(tag))
                .map(tag -> {
                    TwinTagEntity newTag = new TwinTagEntity(); // Builder could allow to map directly without creating a new instance
                    newTag.setTwin(twinEntity);
                    newTag.setTwinId(twinEntity.getId());

                    DataListOptionEntity option = new DataListOptionEntity();
                    option.setOption(tag);
                    option.setBusinessAccountId(twinEntity.getOwnerBusinessAccountId());
                    newTag.setTagDataListOption(option);

                    tagOptions.add(option);

                    return newTag;
                })
                .collect(Collectors.toList());

        // TODO: FIXME: this is only required like that , because we don't have a wrapper function to use in stream
        for (TwinTagEntity tag : tagsToSave) {
            validateEntityAndThrow(tag, EntitySmartService.EntityValidateMode.beforeSave);
        }

        entitySmartService.saveAllAndLog(tagsToSave, twinTagRepository);
        twinEntity.setTwinTagKit(new Kit<>(tagOptions, DataListOptionEntity::getId)); // TODO: FIXME: is this really ok to have side effects, which may not be actually expected ???
    }

    public void deleteTags(TwinEntity twinEntity, Set<UUID> tags) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
}
