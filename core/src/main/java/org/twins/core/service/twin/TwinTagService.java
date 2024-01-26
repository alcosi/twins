package org.twins.core.service.twin;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.Kit;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTagEntity;
import org.twins.core.dao.twin.TwinTagRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
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

    public Kit<DataListOptionEntity> createTags(TwinEntity twinEntity, Set<String> newTags, Set<UUID> existingTags) throws ServiceException {
        return saveTags(twinEntity,
                Optional.ofNullable(newTags)
                        .orElse(new HashSet<>()),
                Optional.ofNullable(existingTags)
                        .orElse(new HashSet<>()));
    }

    public void removeTags(UUID twinId, Set<UUID> tags) {
        log.info(String.format("removeTags(%s, %s)", twinId, tags.toString()));

        Optional.ofNullable(tags)
                .ifPresent(tagsToRemove -> twinTagRepository.deleteByTwinIdAndTagDataListOptionIdIn(twinId, tagsToRemove));
    }

    public void updateTwinTags(TwinEntity twinEntity, Set<UUID> tagsToRemove, Set<String> newTags, Set<UUID> existingTags) throws ServiceException {
        removeTags(twinEntity.getId(), tagsToRemove);
        createTags(twinEntity, newTags, existingTags);
    }

    private Kit<DataListOptionEntity> saveTags(TwinEntity twinEntity, Set<String> newTags, Set<UUID> existingTags) throws ServiceException {
        UUID businessAccountId = authService.getApiUser().getBusinessAccount().getId();

        List<DataListOptionEntity> tagOptions = saveTagOptions(twinEntity.getTwinClass().getTagDataListId(), newTags, businessAccountId);
        List<UUID> filteredTags = filterTagsByBusinessAccount(existingTags, businessAccountId);

        List<TwinTagEntity> tagsToSave = new ArrayList<>();

        tagOptions.forEach(option -> tagsToSave.add(createTagEntity(twinEntity, option.getId(), option)));
        filteredTags.forEach(optionId -> tagsToSave.add(createTagEntity(twinEntity, optionId, null)));

        // remove duplicates if any
        List<TwinTagEntity> distinctTags = tagsToSave.stream()
                .collect(Collectors.toMap(TwinTagEntity::getTagDataListOptionId,
                        Function.identity(),
                        (first, second) -> first))
                .values()
                .stream()
                .collect(Collectors.toList());

        for (TwinTagEntity tag : distinctTags) {
            validateEntityAndThrow(tag, EntitySmartService.EntityValidateMode.beforeSave);
        }

        entitySmartService.saveAllAndLog(distinctTags, twinTagRepository);

        return new Kit<>(tagOptions, DataListOptionEntity::getId);
    }

    private List<DataListOptionEntity> saveTagOptions(UUID dataListId, Set<String> newTagOptions, UUID businessAccountId) {
        List<DataListOptionEntity> tagOptions = new ArrayList<>();

        List<DataListOptionEntity> optionsToSave = newTagOptions.stream().filter(option -> { // save only new options
                DataListOptionEntity foundOption = twinTagRepository.findOptionByTagName(option.trim());

                if (foundOption != null) {
                    tagOptions.add(foundOption);
                    return false;
                }

                return true;
            }).map(tag -> {
                DataListOptionEntity option = new DataListOptionEntity();
                option.setOption(tag);
                option.setBusinessAccountId(businessAccountId);
                option.setStatus(DataListOptionEntity.Status.active);
                option.setDataListId(dataListId);

                return option;
            })
            .collect(Collectors.toList());

        // TODO: FIXME: there is no validator for data list options.

        Iterable<DataListOptionEntity> savedOptions = entitySmartService.saveAllAndLog(optionsToSave, dataListOptionRepository);
        savedOptions.forEach(tagOptions::add);

        return tagOptions;
    }

    private List<UUID> filterTagsByBusinessAccount(Set<UUID> existingTags, UUID businessAccountId) {
        return existingTags.stream()
                .filter(tag -> twinTagRepository.isTagOptionValid(tag, businessAccountId))
                .collect(Collectors.toList());
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
