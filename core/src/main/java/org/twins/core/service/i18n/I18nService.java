package org.twins.core.service.i18n;


import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CacheUtils;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.config.i18n.I18nProperties;
import org.twins.core.dao.AdvancedEntityManager;
import org.twins.core.dao.i18n.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.exception.i18n.ErrorCodeI18n;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Component
@Slf4j
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class I18nService extends EntitySecureFindServiceImpl<I18nEntity> {
    private final I18nRepository i18nRepository;
    private final I18nTranslationRepository i18nTranslationRepository;
    private final I18nTranslationBinRepository i18nTranslationBinRepository;
    private final I18nTranslationStyleRepository i18nTranslationStyleRepository;
    private final I18nProperties i18nProperties;
    @PersistenceContext
    private final EntityManager entityManager;
    private final CacheManager cacheManager;
    private final AuthService authService;
    @Lazy
    private final DomainService domainService;
    private final AdvancedEntityManager advancedEntityManager;

    @Override
    public CrudRepository<I18nEntity, UUID> entityRepository() {
        return i18nRepository;
    }

    @Override
    public Function<I18nEntity, UUID> entityGetIdFunction() {
        return I18nEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(I18nEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo think over domainId validation, some i18n with filled domainId should be accessible from public controllers (datalists)
        return false;
    }

    @Override
    public boolean validateEntity(I18nEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getDomainId() != null && (entity.getDomain() == null || !entity.getDomainId().equals(entity.getDomain().getId())))
                    entity.setDomain(domainService.findEntitySafe(entity.getDomainId()));
        }
        return true;
    }

    public String translateToLocale(I18nEntity i18NEntity, Locale locale) {
        return translateToLocale(i18NEntity, locale, null);
    }

    public String translateToLocaleOrEmpty(I18nEntity i18NEntity, Locale locale) {
        if (i18NEntity == null)
            return "";
        return translateToLocale(i18NEntity, locale, null);
    }

    public String translateToLocale(I18nEntity i18NEntity, Locale locale, Map<String, String> context) {
        return translateToLocale(i18NEntity.getId(), locale, context);
    }

    public String translateToLocale(String i18nKey, Locale locale) throws ServiceException {
        I18nEntity i18NEntity = i18nRepository.findByKey(i18nKey);
        if (i18NEntity == null)
            throw new ServiceException(ErrorCodeI18n.INCORRECT_CONFIGURATION, "Wrong i18n key: " + i18nKey);
        return translateToLocale(i18NEntity, locale, null);
    }

    public String translateToLocale(UUID i18nId, Locale locale, Map<String, String> context) {
        I18nTranslationEntity i18nTranslationEntity = getTranslationEntity(i18nId, locale, context);
        if (i18nTranslationEntity == null) {
            return "";
        }
        String ret = i18nTranslationEntity.getTranslation();
        if (StringUtils.isNotBlank(ret) && MapUtils.isNotEmpty(context))
            return StringUtils.replaceVariables(ret, context);
        else
            return ret;
    }

    public I18nTranslationEntity getTranslationEntity(UUID i18nId, Locale locale) {
        return getTranslationEntity(i18nId, locale, null);
    }

    public I18nTranslationEntity getTranslationEntity(UUID i18nId, Locale locale, Map<String, String> context) {
        if (i18nId == null) {
            log.warn("I18n not configured");
            return null;
        }
        Locale defaultLocale = resolveDefaultLocale();
        Optional<I18nTranslationEntity> i18nTranslationEntity;
        if (locale != null) {
            i18nTranslationEntity = i18nTranslationRepository.findByI18nIdAndLocale(i18nId, locale);
            if (i18nTranslationEntity.isPresent() && StringUtils.isNotBlank(i18nTranslationEntity.get().getTranslation()))
                return i18nTranslationEntity.get();
            else {
                log.info("I18n[{}] translation is missing for locale[{}]", i18nId, locale);
                i18nTranslationRepository.incrementUsageCounter(i18nId, locale.getLanguage());
            }
        }
        //if not translation was found for given locale we will load it for default
        if (defaultLocale != null && !defaultLocale.equals(locale)) {
            i18nTranslationEntity = i18nTranslationRepository.findByI18nIdAndLocale(i18nId, defaultLocale);
            if (i18nTranslationEntity.isPresent())
                return i18nTranslationEntity.get();
        }
        return null;
    }

    public String translateBase24ToLocale(I18nEntity i18NEntity, Locale locale) throws ServiceException {
        Optional<I18nTranslationBinEntity> translationBinEntity = i18nTranslationBinRepository.findByI18nIdAndLocale(i18NEntity.getId(), locale);
        if (translationBinEntity.isEmpty() || translationBinEntity.get().getTranslation().length == 0)
            translationBinEntity = i18nTranslationBinRepository.findByI18nIdAndLocale(i18NEntity.getId(), resolveDefaultLocale());
        if (translationBinEntity.isPresent() && translationBinEntity.get().getTranslation().length > 0)
            return translationBinEntity.get().getBase64();
        return null;
    }


    public String translateToLocaleOrEmpty(String i18nKey, Locale locale) {
        try {
            return translateToLocale(i18nKey, locale);
        } catch (Exception e) {
            return "";
        }
    }

    public void loadTranslations(I18nEntity i18nEntity, String locale) {
        if (i18nEntity == null)
            return;
        List<Locale> locales = Arrays.asList(resolveDefaultLocale(), Locale.forLanguageTag(locale));
        if (i18nEntity.getType().isImage()) {
            i18nEntity.setTranslationsBin(new Kit<>(i18nTranslationBinRepository.findByI18nIdAndLocaleIn(i18nEntity.getId(), locales), I18nTranslationBinEntity::getLocale));
        } else {
            i18nEntity.setTranslationsKit(new Kit<>(i18nTranslationRepository.findByI18nAndLocaleIn(i18nEntity, locales), I18nTranslationEntity::getLocale));
            if (i18nEntity.getType().isStyledText()) {
                for (I18nTranslationEntity translation : i18nEntity.getTranslationsKit().getCollection()) {
                    translation.setStyles(i18nTranslationStyleRepository.findByI18nIdAndLocale(translation.getI18n().getId(), translation.getLocale()));
                }
            }
        }
    }

    public void loadTranslations(I18nEntity i18nEntity) {
        loadTranslations(Collections.singletonList(i18nEntity));
    }

    public void loadTranslations(Collection<I18nEntity> i18nEntityList) {
        loadKit(i18nEntityList,
                I18nEntity::getId,
                I18nEntity::getTranslationsKit,
                I18nEntity::setTranslationsKit,
                i18nTranslationRepository::findByI18nIdIn,
                I18nTranslationEntity::getLocale,
                I18nTranslationEntity::getI18nId);
    }

    public void loadStyles(I18nTranslationEntity translation) {
        if (translation.getI18n().getType().isStyledText()) {
            translation.setStyles(i18nTranslationStyleRepository.findByI18nIdAndLocale(translation.getI18n().getId(), translation.getLocale()));
        }
    }

    public List<I18nTranslationStyleEntity> getStyles(I18nEntity i18nEntity, String locale) {
        if (i18nEntity.getType().isStyledText()) {
            return i18nTranslationStyleRepository.findByI18nIdAndLocale(i18nEntity.getId(), Locale.forLanguageTag(locale));
        }
        return new ArrayList<>();
    }

    public String translateToLocale(I18nEntity i18NEntity, Map<String, String> context) {
        return translateToLocale(i18NEntity, resolveCurrentUserLocale(), context);
    }

    public String translateToLocale(UUID i18nId, Map<String, String> context) {
        return translateToLocale(i18nId, resolveCurrentUserLocale(), context);
    }

    public String translateToLocale(I18nEntity i18NEntity) {
        return translateToLocale(i18NEntity, resolveCurrentUserLocale(), null);
    }

    public String translateToLocale(UUID i18nId) {
        return translateToLocale(i18nId, resolveCurrentUserLocale(), null);
    }

    public Map<UUID, String> translateToLocale(Set<UUID> idsToLoad) {
        if (CollectionUtils.isEmpty(idsToLoad)) {
            return Collections.emptyMap();
        }
        if (idsToLoad.size() == 1) {
            UUID id = idsToLoad.iterator().next();
            return Map.of(id, translateToLocale(id));
        }

        Map<UUID, String> result = new HashMap<>();
        Locale locale = resolveCurrentUserLocale();
        String arrayLiteral = advancedEntityManager.buildPostgresUuidArrayLiteral(idsToLoad);

        i18nTranslationRepository.findByI18nIdInAndLocaleArray(arrayLiteral, locale.toLanguageTag())
                .forEach(t -> result.put(t.i18nId(), t.translation()));

        // Fill missing IDs with empty strings
        idsToLoad.stream()
                .filter(id -> !result.containsKey(id))
                .forEach(id -> result.put(id, ""));

        return result;
    }

    /**
     * Batch i18n duplication: builds copies for every {@code srcId → dstId} pair in {@code remap}.
     * Two bulk reads (i18n + translations) + two bulk writes — total of 4 SQL regardless of remap
     * size. Called by {@code EntityDuplicateService.commit} as a pre-commit step, before the
     * topological sort of entity-class commits, so all i18n rows land in db before any referencer.
     */
    @Transactional(rollbackFor = Throwable.class)
    public void duplicateTranslations(Map<UUID, UUID> remap) {
        if (remap == null || remap.isEmpty()) {
            return;
        }
        List<I18nEntity> srcEntities = new ArrayList<>();
        i18nRepository.findAllById(remap.keySet()).forEach(srcEntities::add);
        List<I18nTranslationEntity> srcTranslations = i18nTranslationRepository.findByI18nIdIn(remap.keySet());
        List<I18nEntity> dstEntities = new ArrayList<>(srcEntities.size());
        for (I18nEntity src : srcEntities) {
            UUID dstId = remap.get(src.getId());
            if (dstId == null) {
                continue;
            }
            dstEntities.add(new I18nEntity()
                    .setId(dstId)
                    .setKey(addCopyPostfix(src.getKey()))
                    .setName(addCopyPostfix(src.getName()))
                    .setType(src.getType())
                    .setDomainId(src.getDomainId()));
        }
        List<I18nTranslationEntity> dstTranslations = new ArrayList<>(srcTranslations.size());
        for (I18nTranslationEntity src : srcTranslations) {
            UUID dstI18nId = remap.get(src.getI18nId());
            if (dstI18nId == null) {
                continue;
            }
            dstTranslations.add(new I18nTranslationEntity()
                    .setI18nId(dstI18nId)
                    .setLocale(src.getLocale())
                    .setTranslation(src.getTranslation()));
        }
        i18nRepository.saveAll(dstEntities);
        i18nTranslationRepository.saveAll(dstTranslations);
    }

    @Transactional(rollbackFor = Throwable.class)
    public I18nEntity createI18nAndDefaultTranslation(I18nType i18nType, String defaultLocaleTranslation) throws ServiceException {
        return createI18nAndTranslations(i18nType, buildI18nEntity(i18nType, defaultLocaleTranslation));
    }

    public I18nEntity buildI18nEntity(I18nType i18nType, String translationInDefaultLocale) {
        return new I18nEntity()
                .setType(i18nType)
                .addTranslation(new I18nTranslationEntity()
                        .setLocale(resolveDefaultLocale())
                        .setTranslation(translationInDefaultLocale));
    }

    @Transactional(rollbackFor = Throwable.class)
    public I18nEntity createI18nAndTranslations(I18nType i18nType, I18nEntity i18nEntity) throws ServiceException {
        if (null == i18nType)
            throw new ServiceException(ErrorCodeI18n.INCORRECT_CONFIGURATION, "i18n type not specified");
        if (null == i18nEntity)
            i18nEntity = new I18nEntity();
//        if (!i18nType.equals(i18nEntity.getType()))
//            throw new ServiceException(ErrorCodeI18n.INCORRECT_CONFIGURATION, "i18n type mismatch");
        i18nEntity
                .setType(i18nType)
                .setId(null)
                .setId(i18nRepository.save(i18nEntity).getId())
                .setDomainId(resolveCurrentDomainId());
        if (KitUtils.isEmpty(i18nEntity.getTranslationsKit()))
            return i18nEntity;
        for (var entry : i18nEntity.getTranslationsKit().getCollection()) {
            entry
                    .setI18n(i18nEntity)
                    .setI18nId(i18nEntity.getId())
                    .setTranslation(StringUtils.defaultString(entry.getTranslation()));
        }
        i18nTranslationRepository.saveAll(i18nEntity.getTranslationsKit().getCollection());
        return i18nEntity;
    }

    public List<I18nEntity> createI18nAndTranslations(I18nType i18nType, List<I18nEntity> i18nEntities) throws ServiceException {
        if (null == i18nType)
            throw new ServiceException(ErrorCodeI18n.INCORRECT_CONFIGURATION, "i18n type not specified");
        UUID domainId = resolveCurrentDomainId();
        i18nEntities.forEach(i -> {
            i.setType(i18nType);
            i.setId(null);
            i.setDomainId(domainId);
        });
        var saved = StreamSupport.stream(i18nRepository.saveAll(i18nEntities).spliterator(), false).toList();
        var translations = saved.stream().flatMap(i18nEntity -> {
            var kit = i18nEntity.getTranslationsKit();
            if (kit != null) {
                return kit.getList().stream().peek(t -> {
                    t.setI18nId(i18nEntity.getId());
                    t.setI18n(i18nEntity);
                    t.setTranslation(StringUtils.defaultString(t.getTranslation()));
                });
            } else {
                return Stream.empty();
            }
        }).toList();
        i18nTranslationRepository.saveAll(translations);
        return i18nEntities;
    }

    @Transactional
    public void createI18nAndTranslationsLight(Collection<I18nTranslationLight> translations) {
        UUID domainId = resolveCurrentDomainId();
        List<I18nEntity> i18nEntities = translations.stream()
                .map(light -> new I18nEntity()
                        .setId(light.i18nId())
                        .setType(light.i18nType())
                        .setDomainId(domainId))
                .toList();

        List<I18nTranslationEntity> translationEntities = translations.stream()
                .map(light -> new I18nTranslationEntity()
                        .setI18nId(light.i18nId())
                        .setLocale(light.locale())
                        .setTranslation(light.translation()))
                .toList();

        i18nRepository.saveAll(i18nEntities);
        i18nTranslationRepository.saveAll(translationEntities);
    }

    private String addCopyPostfix(String originalStr) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(originalStr))
            return originalStr;
        return originalStr + " [copy]";
    }

    public UUID resolveCurrentDomainId() {
        ApiUser apiUser;
        try {
            apiUser = authService.getApiUser();
            return apiUser.getDomainId();
        } catch (ServiceException e) {
            return null;
        }
    }

    public Locale resolveCurrentUserLocale() {
        ApiUser apiUser;
        try {
            apiUser = authService.getApiUser();
            return apiUser.getLocale();
        } catch (ServiceException e) {
            return null;
        }
    }


    public Locale resolveDefaultLocale() {
        ApiUser apiUser;
        try {
            apiUser = authService.getApiUser();
            Locale domainLocale = null;
            if (apiUser.isDomainSpecified())
                domainLocale = apiUser.getDomain().getDefaultI18nLocaleId();
            return domainLocale != null ? domainLocale : i18nProperties.defaultLocale();
        } catch (ServiceException e) {
            return i18nProperties.defaultLocale();
        }
    }

    public Locale localeFromTagOrSystemDefault(String langTag) {
        Locale locale;
        try {
            locale = Locale.forLanguageTag(langTag);
        } catch (Exception e) {
            locale = i18nProperties.defaultLocale();
        }
        return locale;
    }

    @Transactional
    public I18nEntity saveTranslations(I18nType i18nType, I18nEntity i18nEntity) throws ServiceException {
        if (i18nEntity.getId() == null)
            return createI18nAndTranslations(i18nType, i18nEntity);
        else
            return updateTranslations(i18nEntity);
    }

    @Transactional
    public I18nEntity updateTranslations(I18nEntity i18nEntity) throws ServiceException {
        if (i18nEntity.getId() == null)
            throw new ServiceException(ErrorCodeCommon.ENTITY_INVALID, "id can not be empty");
        if (KitUtils.isEmpty(i18nEntity.getTranslationsKit()))
            return i18nEntity;
        //todo all translations are currently being updated. you can update only the ones you need (obtain from the database)
        List<I18nTranslationEntity> entitiesToSave = new ArrayList<>();
        for (var entry : i18nEntity.getTranslationsKit().getMap().entrySet()) {
            if (entry.getValue().getTranslation() == null)
                continue;
            if (entry.getValue().getI18nId() == null) {
                entry.getValue().setI18nId(i18nEntity.getId());
            }
            entitiesToSave.add(entry.getValue());
            CacheUtils.evictCache(cacheManager, I18nTranslationRepository.CACHE_I18N_TRANSLATIONS, entry.getValue().getI18nId() + "" + entry.getKey());
        }
        i18nTranslationRepository.saveAll(entitiesToSave);

        return i18nEntity;
    }

    @Transactional
    public <T> void updateI18nFieldForEntity(I18nEntity i18nEntity, I18nType i18nType, T targetEntity, Function<T, UUID> fieldGetter, BiConsumer<T, UUID> fieldSetter, String fieldName, ChangesHelper changesHelper) throws ServiceException {
        if (i18nEntity == null) {
            return;
        }
        UUID currentI18nId = fieldGetter.apply(targetEntity);

        if (currentI18nId != null) {
            i18nEntity.setId(currentI18nId);
        }

        I18nEntity savedI18n = saveTranslations(i18nType, i18nEntity);

        if (changesHelper.isChanged(fieldName, currentI18nId, savedI18n.getId())) {
            fieldSetter.accept(targetEntity, savedI18n.getId());
        }
    }

    /**
     * Collects i18n IDs from a collection of entities.
     *
     * @param items the collection of entities
     * @param nameI18nExtractor function to extract name i18n ID from entity
     * @param descriptionI18nExtractor function to extract description i18n ID from entity
     * @param <T> the entity type
     * @return set of collected i18n IDs
     */
    public <T> Set<UUID> collectI18nIds(Collection<T> items,
                                         Function<T, UUID> nameI18nExtractor,
                                         Function<T, UUID> descriptionI18nExtractor) {
        Set<UUID> i18nIds = new HashSet<>();
        for (T item : items) {
            UUID nameI18nId = nameI18nExtractor.apply(item);
            if (nameI18nId != null) {
                i18nIds.add(nameI18nId);
            }
            UUID descI18nId = descriptionI18nExtractor.apply(item);
            if (descI18nId != null) {
                i18nIds.add(descI18nId);
            }
        }
        return i18nIds;
    }
}
