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
import org.twins.core.dao.i18n.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.exception.i18n.ErrorCodeI18n;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.*;
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
        Optional<I18nTranslationBinEntity> translationBinEntity = i18nTranslationBinRepository.findByI18nAndLocale(i18NEntity, locale);
        if (translationBinEntity.isEmpty() || translationBinEntity.get().getTranslation().length == 0)
            translationBinEntity = i18nTranslationBinRepository.findByI18nAndLocale(i18NEntity, resolveDefaultLocale());
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
            i18nEntity.setTranslationsBin(new Kit<>(i18nTranslationBinRepository.findByI18nAndLocaleIn(i18nEntity, locales), I18nTranslationBinEntity::getLocale));
        } else {
            i18nEntity.setTranslationsKit(new Kit<>(i18nTranslationRepository.findByI18nAndLocaleIn(i18nEntity, locales), I18nTranslationEntity::getLocale));
            if (i18nEntity.getType().isStyledText()) {
                for (I18nTranslationEntity translation : i18nEntity.getTranslationsKit().getCollection()) {
                    translation.setStyles(i18nTranslationStyleRepository.findByI18nAndLocale(translation.getI18n(), translation.getLocale()));
                }
            }
        }
    }

    public void loadTranslations(I18nEntity i18nEntity) {
        loadTranslations(Collections.singletonList(i18nEntity));
    }

    public void loadTranslations(Collection<I18nEntity> i18nEntityList) {
        Kit<I18nEntity, UUID> needLoad = new Kit<>(I18nEntity::getId);
        for (var i18nEntity : i18nEntityList) {
            if (i18nEntity.getTranslationsKit() != null)
                continue;
            i18nEntity.setTranslationsKit(new Kit<>(I18nTranslationEntity::getLocale));
            needLoad.add(i18nEntity);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        List<I18nTranslationEntity> translations = i18nTranslationRepository.findByI18nIdIn(needLoad.getIdSet());
        if (CollectionUtils.isEmpty(translations))
            return;
        for (var translation : translations) {
            needLoad.get(translation.getI18nId()).addTranslation(translation);
        }
        //todo load styles
    }

    public void loadStyles(I18nTranslationEntity translation) {
        if (translation.getI18n().getType().isStyledText()) {
            translation.setStyles(i18nTranslationStyleRepository.findByI18nAndLocale(translation.getI18n(), translation.getLocale()));
        }
    }

    public List<I18nTranslationStyleEntity> getStyles(I18nEntity i18nEntity, String locale) {
        if (i18nEntity.getType().isStyledText()) {
            return i18nTranslationStyleRepository.findByI18nAndLocale(i18nEntity, Locale.forLanguageTag(locale));
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
        } else if (idsToLoad.size() == 1) {
            UUID id = idsToLoad.iterator().next();
            return Map.of(id, translateToLocale(id));
        } else {
            Map<UUID, String> result = new HashMap<>();
            Locale locale = resolveCurrentUserLocale();
            i18nTranslationRepository.findByI18nIdInAndLocale(idsToLoad, locale).forEach(t -> result.put(t.getI18nId(), t.getTranslation()));
            if (idsToLoad.size() != result.size()) {
                idsToLoad.stream().filter(id -> !result.containsKey(id)).forEach(id -> result.put(id, ""));
            }
            return result;
        }
    }

    @Transactional
    public I18nEntity duplicateI18n(UUID srcI18nId) {
        return duplicateI18n(i18nRepository.findById(srcI18nId).get());
    }

    @Transactional(rollbackFor = Throwable.class)
    public I18nEntity duplicateI18n(I18nEntity srcI18nEntity) {
        I18nEntity duplicateI18nEntity = new I18nEntity()
                .setKey(addCopyPostfix(srcI18nEntity.getKey()))
                .setName(addCopyPostfix(srcI18nEntity.getName()))
                .setType(srcI18nEntity.getType());
        duplicateI18nEntity = i18nRepository.save(duplicateI18nEntity);
        List<I18nTranslationEntity> translationEntityList = i18nTranslationRepository.findByI18nId(srcI18nEntity.getId());
        if (CollectionUtils.isNotEmpty(translationEntityList)) {
            List<I18nTranslationEntity> duplicateI18nTranslationEntityList = new ArrayList<>();
            for (I18nTranslationEntity srcI18nTranslationEntity : translationEntityList) {
                duplicateI18nTranslationEntityList.add(
                        new I18nTranslationEntity()
                                .setI18nId(duplicateI18nEntity.getId())
                                .setLocale(srcI18nTranslationEntity.getLocale())
                                .setTranslation(srcI18nTranslationEntity.getTranslation()));
            }
            i18nTranslationRepository.saveAll(duplicateI18nTranslationEntityList);
        }
        return entityManager.merge(duplicateI18nEntity);
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
}
