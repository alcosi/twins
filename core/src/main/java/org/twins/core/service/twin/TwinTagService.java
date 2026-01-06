package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTagEntity;
import org.twins.core.dao.twin.TwinTagRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.enums.EntityRelinkOperationStrategy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinTagService extends EntitySecureFindServiceImpl<TwinTagEntity> {
    final TwinTagRepository twinTagRepository;
    final TwinService twinService;
    final DataListService dataListService;
    final AuthService authService;
    private final DataListOptionService dataListOptionService;

    @Override
    public CrudRepository<TwinTagEntity, UUID> entityRepository() {
        return twinTagRepository;
    }

    @Override
    public Function<TwinTagEntity, UUID> entityGetIdFunction() {
        return TwinTagEntity::getId;
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
                UUID expectedTagDataListId = entity.getTwin().getTwinClass().getTagDataListId() != null
                        ? entity.getTwin().getTwinClass().getTagDataListId()
                        : entity.getTwin().getTwinClass().getInheritedTagDataListId();

                if (!expectedTagDataListId.equals(entity.getTagDataListOption().getDataListId()))
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

    public void createTags(TwinEntity twinEntity, Set<String> newTags, Set<UUID> existingTags, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(newTags) && CollectionUtils.isEmpty(existingTags))
            return;
        if (twinEntity.getTwinClass().getTagDataListId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_TAGS_NOT_ALLOWED, "tags are not allowed for " + twinEntity.logNormal());
        saveTags(twinEntity, newTags, existingTags, twinChangesCollector);
    }

    public void removeTags(TwinEntity twinEntity, Set<UUID> tagsDelete, TwinChangesCollector twinChangesCollector) {
        if (CollectionUtils.isEmpty(tagsDelete))
            return;
        // it's not possible to delete it in such way, because we need to write history
        // twinTagRepository.deleteByTwinIdAndTagDataListOptionIdIn(twinEntity.getId(), tags);
        List<TwinTagEntity> tags = twinTagRepository.findAllByTwinIdAndTagDataListOptionIdIn(twinEntity.getId(), tagsDelete);
        if(tags.size() != tagsDelete.size()) {
            log.warn("Mismatch markers for deletion with existing: markers (IDs: {}) and markersDelete (IDs: {}).",
                    tags.stream().map(TwinTagEntity::getId).collect(Collectors.toSet()),
                    tagsDelete);
        }
        //todo add history
        for (TwinTagEntity tag : tags)
            twinChangesCollector.delete(tag);
    }

    public void updateTwinTags(TwinEntity twinEntity, Set<UUID> tagsToRemove, Set<String> newTags, Set<UUID> existingTags, TwinChangesCollector twinChangesCollector) throws ServiceException {
        removeTags(twinEntity, tagsToRemove, twinChangesCollector);
        createTags(twinEntity, newTags, existingTags, twinChangesCollector);
    }

    private void saveTags(TwinEntity twinEntity, Set<String> newTagsStrings, Set<UUID> existingTagsIds, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(newTagsStrings) && CollectionUtils.isEmpty(existingTagsIds))
            return;
        UUID businessAccountId = null;
        if (authService.getApiUser().isBusinessAccountSpecified())
            businessAccountId = authService.getApiUser().getBusinessAccountId();
        Kit<TwinTagEntity, UUID> tagsToSave = new Kit<>(TwinTagEntity::getTagDataListOptionId); //we will use kit to guaranty uniq
        if (CollectionUtils.isNotEmpty(newTagsStrings)) {
            List<DataListOptionEntity> newTags = dataListOptionService.processNewOptions(twinEntity.getTwinClass().getTagDataListId(), newTagsStrings, businessAccountId);
            for (var option : newTags) {
                tagsToSave.add(createTagEntity(twinEntity, option.getId(), option));
            }
        }
        if (CollectionUtils.isNotEmpty(existingTagsIds)) {
            List<DataListOptionEntity> existingTags;
            if (businessAccountId != null)
                existingTags = twinTagRepository.findForBusinessAccount(twinEntity.getTwinClass().getTagDataListId(), businessAccountId, existingTagsIds);
            else
                existingTags = twinTagRepository.findTagsOutOfBusinessAccount(twinEntity.getTwinClass().getTagDataListId(), existingTagsIds);
            for (var option : existingTags) {
                tagsToSave.add(createTagEntity(twinEntity, option.getId(), option));
            }
        }
        if (KitUtils.isEmpty(tagsToSave))
            return;
        loadTags(twinEntity);
        for (TwinTagEntity tagToSave : tagsToSave.getMap().values()) {
            if (!twinEntity.getTwinTagKit().containsKey(tagToSave.getTagDataListOptionId())) {
                //todo add history
                twinChangesCollector.add(tagToSave);
            }
        }
    }

    private TwinTagEntity createTagEntity(TwinEntity twinEntity, UUID optionId, DataListOptionEntity option) throws ServiceException {
        TwinTagEntity newTag = new TwinTagEntity();
        newTag.setTwin(twinEntity);
        newTag.setTwinId(twinEntity.getId());
        newTag.setTagDataListOptionId(optionId);
        newTag.setTagDataListOption(option);
        validateEntityAndThrow(newTag, EntitySmartService.EntityValidateMode.beforeSave);
        return newTag;
    }

    public void deleteAllTagsForTwinsOfClass(UUID twinClassId) {
        twinTagRepository.deleteByTwin_TwinClassId(twinClassId);
    }

    public void replaceTagsForTwinsOfClass(TwinClassEntity twinClassEntity, EntityRelinkOperation entityRelinkOperation) throws ServiceException {
        if (UuidUtils.isNullifyMarker(entityRelinkOperation.getNewId())) {
            //we have to delete all tags from twins of given class
            deleteAllTagsForTwinsOfClass(twinClassEntity.getId());
            twinClassEntity
                    .setTagDataListId(null)
                    .setTagDataList(null);
            return;
        }
        DataListEntity newTagsDataList = dataListService.findEntitySafe(entityRelinkOperation.getNewId());
        //we will try to replace tags with new provided values
        Set<UUID> existedTwinTagIds = findExistedTwinTagsForTwinsOfClass(twinClassEntity.getId());
        if (CollectionUtils.isEmpty(existedTwinTagIds)) {
            twinClassEntity
                    .setTagDataList(newTagsDataList)
                    .setTagDataListId(newTagsDataList.getId());
            return;
        }
        if (entityRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict
                && MapUtils.isEmpty(entityRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide tagsReplaceMap for tags: " + org.cambium.common.util.StringUtils.join(existedTwinTagIds));

        dataListService.loadDataListOptions(newTagsDataList);
        Set<UUID> tagsForDeletion = new HashSet<>();
        for (UUID tagForReplace : existedTwinTagIds) {
            if (newTagsDataList.getOptions().get(tagForReplace) != null) //be smart if somehow already existed tag belongs to new list
                continue;
            UUID replacement = entityRelinkOperation.getReplaceMap().get(tagForReplace);
            if (replacement == null) {
                if (entityRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide tagsReplaceMap value for tag: " + tagForReplace);
                else
                    replacement = UuidUtils.NULLIFY_MARKER;
            }
            if (UuidUtils.isNullifyMarker(replacement)) {
                tagsForDeletion.add(tagForReplace);
                continue;
            }
            if (newTagsDataList.getOptions().get(replacement) == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide correct tagsReplaceMap value for tag: " + tagForReplace);
            twinTagRepository.replaceTagsForTwinsOfClass(twinClassEntity.getId(), tagForReplace, replacement);
        }
        if (CollectionUtils.isNotEmpty(tagsForDeletion)) {
            twinTagRepository.deleteByTwin_TwinClassIdAndTagDataListOptionIdIn(twinClassEntity.getId(), tagsForDeletion);
        }
        twinClassEntity
                .setTagDataList(newTagsDataList)
                .setTagDataListId(newTagsDataList.getId());
    }

    private Set<UUID> findExistedTwinTagsForTwinsOfClass(UUID twinClassId) {
        return twinTagRepository.findDistinctTagsDataListOptionIdByTwinTwinClassId(twinClassId);
    }
}
