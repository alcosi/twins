package org.twins.core.service.datalist;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.*;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.i18n.I18nTranslationLight;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.datalist.DataListOptionCreate;
import org.twins.core.domain.datalist.DataListOptionUpdate;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.enums.datalist.DataListStatus;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

//Log calls that took more then 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Service
@RequiredArgsConstructor
public class DataListOptionService extends EntitySecureFindServiceImpl<DataListOptionEntity> {
    final DataListOptionRepository dataListOptionRepository;
    final AuthService authService;
    private final I18nService i18nService;
    final CacheManager cacheManager;

    @Lazy
    @Autowired
    private DataListService dataListService;
    @Autowired
    private DataListOptionSearchService dataListOptionSearchService;

    @Override
    public CrudRepository<DataListOptionEntity, UUID> entityRepository() {
        return dataListOptionRepository;
    }

    @Override
    public Function<DataListOptionEntity, UUID> entityGetIdFunction() {
        return DataListOptionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DataListOptionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainEntity domain = apiUser.getDomain();
        boolean readDenied = (!entity.getDataList().getDomainId().equals(domain.getId()) || (apiUser.isBusinessAccountSpecified()
                && entity.getBusinessAccountId() != null
                && !entity.getBusinessAccountId().equals(apiUser.getBusinessAccount().getId())));
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in " + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(DataListOptionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListOptionEntity createDataListOptions(DataListOptionCreate create) throws ServiceException {
        return createDataListOptions(List.of(create)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListOptionEntity> createDataListOptions(List<DataListOptionCreate> dataListOptionCreates) throws ServiceException {
        if (dataListOptionCreates.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataListOptionEntity> optionsToSave = new ArrayList<>();
        //Find all entities
        Kit<DataListEntity, UUID> dataListsKit = dataListService.findEntitiesSafe(dataListOptionCreates.stream().map(DataListOptionCreate::getDataListId).collect(Collectors.toSet()));
        //And it's translations
        i18nService.createI18nAndTranslations(I18nType.DATA_LIST_OPTION_VALUE,
                dataListOptionCreates
                        .stream().map(DataListOptionCreate::getNameI18n)
                        .toList());
        //todo save description
        for (DataListOptionCreate dataListOptionCreate : dataListOptionCreates) {
            DataListEntity dataList = dataListsKit.get(dataListOptionCreate.getDataListId());
            loadDataListAttributeAccessors(dataList);
            DataListOptionEntity dataListOption = new DataListOptionEntity()
                    .setOptionI18nId(dataListOptionCreate.getNameI18n().getId())
                    .setDescriptionI18nId(dataListOptionCreate.getDescriptionI18n() != null ? dataListOptionCreate.getDescriptionI18n().getId() : null)
                    .setDataListId(dataListOptionCreate.getDataListId())
                    .setIcon(dataListOptionCreate.getIcon())
                    .setStatus(DataListStatus.active)
                    .setBackgroundColor(dataListOptionCreate.getBackgroundColor())
                    .setFontColor(dataListOptionCreate.getFontColor())
                    .setExternalId(dataListOptionCreate.getExternalId())
                    .setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            createAttributes(dataList, dataListOption, dataListOptionCreate.getAttributes());
            validateEntityAndThrow(dataListOption, EntitySmartService.EntityValidateMode.beforeSave);
            optionsToSave.add(dataListOption);
        }
        List<DataListOptionEntity> result = StreamSupport.stream(entityRepository().saveAll(optionsToSave).spliterator(), false).toList();
        return result;
    }

    private void createAttributes(DataListEntity dataList, DataListOptionEntity dataListOption, Map<String, String> attributes) throws ServiceException {
        if (emptyAttributes(dataList))
            return;
        String attributeValue;
        for (var attributeAccessor : dataList.getAttributes().entrySet()) {
            attributeValue = getAttributeValueSafe(attributes, attributeAccessor.getKey());
            attributeAccessor.getValue().setter().accept(dataListOption, attributeValue);
        }
    }

    private static String getAttributeValueSafe(Map<String, String> optionAttributes, String attributeKey) throws ServiceException {
        if (StringUtils.isEmpty(attributeKey))
            return null; //no attribute configured
        if (MapUtils.isEmpty(optionAttributes) || !optionAttributes.containsKey(attributeKey))
            throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_INVALID_ATTRIBUTE, "Incorrect data list option attribute, expected [" + attributeKey + "] not found");
        return optionAttributes.get(attributeKey);
    }

    private boolean emptyAttributes(DataListEntity dataListEntity) {
        loadDataListAttributeAccessors(dataListEntity);
        return MapUtils.isEmpty(dataListEntity.getAttributes());
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataListOptionEntity updateDataListOptions(DataListOptionUpdate update) throws ServiceException {
        return updateDataListOptions(List.of(update)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<DataListOptionEntity> updateDataListOptions(List<DataListOptionUpdate> optionUpdates) throws ServiceException {
        if (optionUpdates.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<DataListOptionEntity> changes = new ChangesHelperMulti<>();
        List<DataListOptionEntity> allEntities = new ArrayList<>(optionUpdates.size());

        for (DataListOptionUpdate update : optionUpdates) {
            DataListOptionEntity dbOption = findEntitySafe(update.getId());
            allEntities.add(dbOption);

            DataListEntity dbDataList = dbOption.getDataList();
            loadDataListAttributeAccessors(dbDataList);

            ChangesHelper changesHelper = new ChangesHelper();
            i18nService.updateI18nFieldForEntity(update.getNameI18n(), I18nType.DATA_LIST_OPTION_VALUE, dbOption, DataListOptionEntity::getOptionI18nId, DataListOptionEntity::setOptionI18nId, DataListOptionEntity.Fields.optionI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(update.getDescriptionI18n(), I18nType.DATA_LIST_OPTION_DESCRIPTION, dbOption, DataListOptionEntity::getDescriptionI18nId, DataListOptionEntity::setDescriptionI18nId, DataListOptionEntity.Fields.descriptionI18nId, changesHelper);
            updateAttributes(dbDataList, dbOption, update.getAttributes(), changesHelper);
            updateEntityFieldByValue(update.getIcon(), dbOption, DataListOptionEntity::getIcon, DataListOptionEntity::setIcon, DataListOptionEntity.Fields.icon, changesHelper);
            updateEntityFieldByValue(update.getStatus(), dbOption, DataListOptionEntity::getStatus, DataListOptionEntity::setStatus, DataListOptionEntity.Fields.status, changesHelper);
            updateEntityFieldByValue(update.getExternalId(), dbOption, DataListOptionEntity::getExternalId, DataListOptionEntity::setExternalId, DataListOptionEntity.Fields.externalId, changesHelper);
            updateEntityFieldByValue(update.getBackgroundColor(), dbOption, DataListOptionEntity::getBackgroundColor, DataListOptionEntity::setBackgroundColor, DataListOptionEntity.Fields.backgroundColor, changesHelper);
            updateEntityFieldByValue(update.getFontColor(), dbOption, DataListOptionEntity::getFontColor, DataListOptionEntity::setFontColor, DataListOptionEntity.Fields.fontColor, changesHelper);

            changes.add(dbOption, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

    private void updateAttributes(DataListEntity dataList, DataListOptionEntity option, Map<String, String> attributes, ChangesHelper changesHelper) {
        if (emptyAttributes(dataList) || MapUtils.isEmpty(attributes))
            return;
        String attributeValue;
        for (var attributeAccessor : dataList.getAttributes().entrySet()) {
            if (StringUtils.isEmpty(attributeAccessor.getKey()))
                continue;
            if (MapUtils.isEmpty(attributes) || !attributes.containsKey(attributeAccessor.getKey()))
                continue;
            attributeValue = attributes.get(attributeAccessor.getKey());
            updateDataListOptionAttribute(attributeValue, attributeAccessor.getKey(), option, attributeAccessor.getValue().getter(), attributeAccessor.getValue().setter(), changesHelper);
        }
    }

    private void loadDataListAttributeAccessors(DataListEntity dataListEntity) {
        if (dataListEntity.getAttributes() != null)
            return;
        Map<String, DataListOptionEntity.AttributeAccessor> attributes = new HashMap<>();
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute1key()))
            attributes.put(dataListEntity.getAttribute1key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute1value, DataListOptionEntity::setAttribute1value));
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute2key()))
            attributes.put(dataListEntity.getAttribute2key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute2value, DataListOptionEntity::setAttribute2value));
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute3key()))
            attributes.put(dataListEntity.getAttribute3key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute3value, DataListOptionEntity::setAttribute3value));
        if (StringUtils.isNotEmpty(dataListEntity.getAttribute4key()))
            attributes.put(dataListEntity.getAttribute4key(), new DataListOptionEntity.AttributeAccessor(DataListOptionEntity::getAttribute4value, DataListOptionEntity::setAttribute4value));
        dataListEntity.setAttributes(attributes);
    }

    private void updateDataListOptionAttribute(String newAttr, String fieldName, DataListOptionEntity dbEntity, Function<DataListOptionEntity, String> getAttr, BiConsumer<DataListOptionEntity, String> setAttr, ChangesHelper changesHelper) {
        if (newAttr == null)
            return;
        String value = getAttr.apply(dbEntity);
        if (!changesHelper.isChanged(fieldName, value, newAttr))
            return;
        setAttr.accept(dbEntity, newAttr);
    }

    public Iterable<DataListOptionEntity> saveOptions(List<DataListOptionEntity> newOptions) {
        return entitySmartService.saveAllAndLog(newOptions, dataListOptionRepository);
    }

    //Method for reloading options if dataList is not present in entity;
    public void reloadOptionsOnDataListAbsent(FieldValueSelect valueSelect) throws ServiceException {
        List<UUID> idsForReload = new ArrayList<>();

        for (var option : valueSelect.getItems()) {
            if (null == option.getDataList() || null == option.getDataListId()) {
                idsForReload.add(option.getId());
            }
        }

        if (!idsForReload.isEmpty()) {
            var loadedOptions = findEntities(idsForReload, EntitySmartService.ListFindMode.ifMissedThrows, EntitySmartService.ReadPermissionCheckMode.none, EntitySmartService.EntityValidateMode.afterRead);
            valueSelect.setItems(loadedOptions);
        }
    }

    public Kit<DataListOptionEntity, UUID> findDataListOptionsByIds(Collection<UUID> dataListOptionIdSet) throws ServiceException {
        List<DataListOptionEntity> dataListOptionEntityList;
        if (authService.getApiUser().isBusinessAccountSpecified())
            dataListOptionEntityList = dataListOptionRepository.findByIdInAndBusinessAccountId(dataListOptionIdSet, authService.getApiUser().getBusinessAccount().getId());
        else
            dataListOptionEntityList = dataListOptionRepository.findByIdIn(dataListOptionIdSet);
        return new Kit<>(dataListOptionEntityList, DataListOptionEntity::getId);
    }


    public void processIncompleteOptions(UUID dataListId, List<DataListOptionEntity> options, UUID businessAccountId, boolean supportCustomValue) throws ServiceException {
        processIncompleteByExternalIdOptions(dataListId, options, businessAccountId, supportCustomValue);
        processIncompleteByKeyOptions(dataListId, options, businessAccountId, supportCustomValue);
    }

    public void processIncompleteByExternalIdOptions(UUID dataListId, List<DataListOptionEntity> options, UUID businessAccountId, boolean supportCustomValue) throws ServiceException {
        Kit<DataListOptionEntity, String> incompleteOptionKit = new Kit<>(DataListOptionEntity::getExternalId);
        Iterator<DataListOptionEntity> iterator = options.iterator();
        while (iterator.hasNext()) {
            DataListOptionEntity option = iterator.next();
            if (option.getId() == null && StringUtils.isNotEmpty(option.getExternalId())) {
                iterator.remove();
                incompleteOptionKit.add(option);
            }
        }
        if (KitUtils.isEmpty(incompleteOptionKit)) {
            return;
        }
        DataListOptionSearch dataListOptionSearch = new DataListOptionSearch()
                .addDataListId(dataListId, false)
                .setExternalIdList(incompleteOptionKit.getIdSet());
        if (businessAccountId != null)
            dataListOptionSearch.addBusinessAccountId(businessAccountId, false);
        Kit<DataListOptionEntity, String> existedOptions = new Kit<>(dataListOptionSearchService.findDataListOptions(dataListOptionSearch), DataListOptionEntity::getExternalId);
        options.addAll(existedOptions.getCollection());

        List<String> missedList = incompleteOptionKit.getIdSet().stream()
                .filter(incomplete -> !existedOptions.containsKey(incomplete))
                .collect(Collectors.toList());

        if (!missedList.isEmpty()) {
            if (supportCustomValue) {
                List<DataListOptionEntity> optionsForSave = new ArrayList<>();
                List<I18nTranslationLight> translationsToSave = new ArrayList<>();

                for (var missed : missedList) {
                    UUID i18nId = UuidUtils.generate();

                    DataListOptionEntity option = incompleteOptionKit.get(missed)
                            .setBusinessAccountId(businessAccountId)
                            .setDataListId(dataListId)
                            .setCustom(true)
                            .setExternalId(missed)
                            .setStatus(DataListStatus.active)
                            .setOptionI18nId(i18nId);

                    optionsForSave.add(option);

                    translationsToSave.add(new I18nTranslationLight(
                            i18nId,
                            I18nType.DATA_LIST_OPTION_VALUE,
                            Locale.ENGLISH,
                            missed
                    ));
                }

                i18nService.createI18nAndTranslationsLight(translationsToSave);

                log.info("Creating {} new datalist options with externalIds: {}", optionsForSave.size(), missedList);
                Iterable<DataListOptionEntity> savedOptions = saveOptions(optionsForSave);
                savedOptions.forEach(options::add);
                evictOptionsCloudCache(dataListId, businessAccountId);
            } else {
                String formattedIds = missedList.stream().collect(Collectors.joining(",", "[", "]"));
                throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_IS_NOT_VALID_FOR_LIST, "unknown external ids" + formattedIds);
            }
        }
    }

    public void processIncompleteByKeyOptions(UUID dataListId, List<DataListOptionEntity> options, UUID businessAccountId, boolean supportCustomValue) throws ServiceException {
        Kit<DataListOptionEntity, String> incompleteOptionKit = new Kit<>(DataListOptionEntity::getOption);
        Iterator<DataListOptionEntity> iterator = options.iterator();
        while (iterator.hasNext()) {
            DataListOptionEntity option = iterator.next();
            if (option.getId() == null && option.getExternalId() == null && StringUtils.isNotEmpty(option.getOption())) {
                iterator.remove();
                incompleteOptionKit.add(option);
            }
        }
        if (KitUtils.isEmpty(incompleteOptionKit)) {
            return;
        }
        DataListOptionSearch dataListOptionSearch = new DataListOptionSearch()
                .addDataListId(dataListId, false)
                .setOptionLikeList(incompleteOptionKit.getIdSet());
        if (businessAccountId != null)
            dataListOptionSearch.addBusinessAccountId(businessAccountId, false);
        Kit<DataListOptionEntity, String> existedOptions = new Kit<>(dataListOptionSearchService.findDataListOptions(dataListOptionSearch), DataListOptionEntity::getOption);
        options.addAll(existedOptions.getCollection());

        List<String> missedList = incompleteOptionKit.getIdSet().stream()
                .filter(incomplete -> !existedOptions.containsKeyIgnoreCase(incomplete))
                .collect(Collectors.toList());

        if (!missedList.isEmpty()) {
            if (supportCustomValue) {
                List<DataListOptionEntity> optionsForSave = new ArrayList<>();
                for (var missed : missedList) {
                    optionsForSave.add(
                            incompleteOptionKit.get(missed)
                                    .setBusinessAccountId(businessAccountId)
                                    .setDataListId(dataListId)
                                    .setCustom(true)
                                    .setOption(missed)
                                    .setStatus(DataListStatus.active));
                }

                log.info("Creating {} new datalist options with optionKey: {}", optionsForSave.size(), missedList);
                Iterable<DataListOptionEntity> savedOptions = saveOptions(optionsForSave);
                savedOptions.forEach(options::add);
                evictOptionsCloudCache(dataListId, businessAccountId);
            } else {
                String formattedIds = missedList.stream().collect(Collectors.joining(",", "[", "]"));
                throw new ServiceException(ErrorCodeTwins.DATALIST_OPTION_IS_NOT_VALID_FOR_LIST, "unknown option" + formattedIds);
            }
        }
    }

    private DataListOptionEntity createNewCustomOption(UUID dataListId, UUID businessAccountId, String option, String externalId) {
        return new DataListOptionEntity()
                .setOption(option)
                .setBusinessAccountId(businessAccountId)
                .setStatus(DataListStatus.active)
                .setDataListId(dataListId)
                .setExternalId(externalId)
                .setCustom(true);
    }

    public List<DataListOptionEntity> processNewOptions(UUID dataListId, Set<String> newOptions, UUID businessAccountId) throws ServiceException {
        List<DataListOptionEntity> options = new ArrayList<>();
        for (String optionName : newOptions) {
            options.add(
                    new DataListOptionEntity()
                            .setDataListId(dataListId)
                            .setOption(optionName));
        }
        processIncompleteByKeyOptions(dataListId, options, businessAccountId, true);
        return options;
    }

    private void evictOptionsCloudCache(UUID dataListId, UUID businessAccountId) {
        Cache cache = cacheManager.getCache(DataListOptionRepository.CACHE_DATA_LIST_OPTIONS);
        if (cache != null)
            cache.evictIfPresent(dataListId);
        if (businessAccountId != null) {
            cache = cacheManager.getCache(DataListOptionRepository.CACHE_DATA_LIST_OPTIONS_WITH_BUSINESS_ACCOUNT);
            if (cache != null)
                cache.evictIfPresent(dataListId + "" + businessAccountId);
        }
    }
    //todo move *options methods from  DataListService
}
