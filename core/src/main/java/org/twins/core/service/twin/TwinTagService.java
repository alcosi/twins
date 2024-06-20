package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTagEntity;
import org.twins.core.dao.twin.TwinTagRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinTagService extends EntitySecureFindServiceImpl<TwinTagEntity> {
    final TwinTagRepository twinTagRepository;
    final DataListOptionRepository dataListOptionRepository;
    final TwinService twinService;
    final DataListService dataListService;
    final EntitySmartService entitySmartService;
    final I18nService i18nService;
    final AuthService authService;
    final CacheManager cacheManager;

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
                    entity.setTwin(twinService.findEntitySafe(entity.getTwinId())); // Why there is a side effect in validate function ?!
                if (entity.getTagDataListOption() == null)
                    entity.setTagDataListOption(dataListService.findDataListOption(entity.getTagDataListOptionId())); // Why there is a side effect in validate function ?!
            default:
                if (!entity.getTwin().getTwinClass().getTagDataListId().equals(entity.getTagDataListOption().getDataListId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect twinTag dataListOptionId[" + entity.getTagDataListOptionId() + "]");
        }
        return true;
    }

    public Kit<DataListOptionEntity, UUID> loadTags(TwinEntity twinEntity) {
        if (twinEntity.getTwinTagKit() != null)
            return twinEntity.getTwinTagKit();
        List<DataListOptionEntity> dataListOptionEntityList = findDataListOptionByTwinId(twinEntity.getId());
        if (dataListOptionEntityList != null)
            twinEntity.setTwinTagKit(new Kit<>(dataListOptionEntityList, DataListOptionEntity::getId));
        return twinEntity.getTwinTagKit();
    }

    public List<DataListOptionEntity> findDataListOptionByTwinId(UUID twinId) {
        return twinTagRepository.findDataListOptionByTwinId(twinId);
    }

    // usage log

    public void loadTags(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getTwinTagKit() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.isEmpty())
            return;
        List<TwinTagEntity> twinTagEntityList = twinTagRepository.findByTwinIdIn(needLoad.keySet());
        if (CollectionUtils.isEmpty(twinTagEntityList))
            return;
        Map<UUID, List<DataListOptionEntity>> tagsMap = new HashMap<>(); // key - twinId
        for (TwinTagEntity twinTagEntity : twinTagEntityList) { //grouping by twin
            tagsMap.computeIfAbsent(twinTagEntity.getTwinId(), k -> new ArrayList<>());
            tagsMap.get(twinTagEntity.getTwinId()).add(twinTagEntity.getTagDataListOption());
        }
        TwinEntity twinEntity;
        List<DataListOptionEntity> twinTags;
        for (Map.Entry<UUID, TwinEntity> entry : needLoad.entrySet()) {
            twinEntity = entry.getValue();
            twinTags = tagsMap.get(entry.getKey());
            twinEntity.setTwinTagKit(new Kit<>(twinTags, DataListOptionEntity::getId));
        }
    }

    public Kit<DataListOptionEntity, UUID> createTags(TwinEntity twinEntity, Set<String> newTags, Set<UUID> existingTags) throws ServiceException {
        if (CollectionUtils.isEmpty(newTags) && CollectionUtils.isEmpty(existingTags))
            return null;
        if (twinEntity.getTwinClass().getTagDataListId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_TAGS_NOT_ALLOWED, "tags are not allowed for " + twinEntity.logNormal());
        Kit<DataListOptionEntity, UUID> savedTags = saveTags(twinEntity,
                Optional.ofNullable(newTags)
                        .orElse(new HashSet<>()),
                Optional.ofNullable(existingTags)
                        .orElse(new HashSet<>()));
        twinEntity.setTwinTagKit(null);
        return savedTags;
    }

    public void removeTags(TwinEntity twinEntity, Set<UUID> tags) {
        if (CollectionUtils.isEmpty(tags))
            return;
        log.info(String.format("%s tags[%s] perhaps will be deleted", twinEntity.getId(), StringUtils.join(tags, ",")));
        twinTagRepository.deleteByTwinIdAndTagDataListOptionIdIn(twinEntity.getId(), tags);
        twinEntity.setTwinTagKit(null);
    }

    public void updateTwinTags(TwinEntity twinEntity, Set<UUID> tagsToRemove, Set<String> newTags, Set<UUID> existingTags) throws ServiceException {
        removeTags(twinEntity, tagsToRemove);
        createTags(twinEntity, newTags, existingTags);
    }

    private Kit<DataListOptionEntity, UUID> saveTags(TwinEntity twinEntity, Set<String> newTags, Set<UUID> existingTags) throws ServiceException {
        UUID businessAccountId = null;

        if (authService.getApiUser().isBusinessAccountSpecified()) {
            businessAccountId = authService.getApiUser().getBusinessAccount().getId();
        }

        List<DataListOptionEntity> tagOptions = dataListService.processNewOptions(twinEntity.getTwinClass().getTagDataListId(), newTags, businessAccountId);
        List<DataListOptionEntity> filteredExistingTags;
        if (businessAccountId != null)
            filteredExistingTags = twinTagRepository.findForBusinessAccount(twinEntity.getTwinClass().getTagDataListId(), businessAccountId, existingTags);
        else
            filteredExistingTags = twinTagRepository.findTagsOutOfBusinessAccount(twinEntity.getTwinClass().getTagDataListId(), existingTags);

        List<TwinTagEntity> tagsToSave = new ArrayList<>();
        tagOptions.forEach(option -> tagsToSave.add(createTagEntity(twinEntity, option.getId(), option)));
        filteredExistingTags.forEach(option -> tagsToSave.add(createTagEntity(twinEntity, option.getId(), null)));

        // remove duplicates if any
        List<TwinTagEntity> distinctTags = new ArrayList<>(tagsToSave.stream()
                .collect(Collectors.toMap(TwinTagEntity::getTagDataListOptionId,
                        Function.identity(),
                        (first, second) -> first))
                .values());

        List<UUID> tagListOptionIds = new ArrayList<>();
        for (TwinTagEntity tag : distinctTags) {
            validateEntityAndThrow(tag, EntitySmartService.EntityValidateMode.beforeSave);
            tagListOptionIds.add(tag.getTagDataListOptionId());
        }
        //  Checking for duplicate tags by twin
        List<TwinTagEntity> existingTagsOption = twinTagRepository.findAllByTwinIdAndTagDataListOptionIdIn(twinEntity.getId(), tagListOptionIds);
        distinctTags.removeIf(distinctTag -> existingTagsOption.stream().anyMatch(existTag -> distinctTag.getTagDataListOptionId().equals(existTag.getTagDataListOptionId())));

        entitySmartService.saveAllAndLog(distinctTags, twinTagRepository);
        return new Kit<>(tagOptions, DataListOptionEntity::getId);
    }

    private TwinTagEntity createTagEntity(TwinEntity twinEntity, UUID optionId, DataListOptionEntity option) {
        TwinTagEntity newTag = new TwinTagEntity();
        newTag.setTwin(twinEntity);
        newTag.setTwinId(twinEntity.getId());
        newTag.setTagDataListOptionId(optionId);
        newTag.setTagDataListOption(option);

        return newTag;
    }
}
