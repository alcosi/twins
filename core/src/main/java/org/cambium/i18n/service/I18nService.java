package org.cambium.i18n.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.cambium.i18n.config.I18nProperties;
import org.cambium.i18n.dao.*;
import org.cambium.i18n.exception.ErrorCodeI18n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Slf4j
public abstract class I18nService {
    @Autowired
    private I18nRepository i18nRepository;
    @Autowired
    private I18nTranslationRepository i18nTranslationRepository;
    @Autowired
    private I18nTranslationBinRepository i18nTranslationBinRepository;
    @Autowired
    private I18nTranslationStyleRepository i18nTranslationStyleRepository;
    @Autowired
    private I18nProperties i18nProperties;
    @PersistenceContext
    private EntityManager entityManager;

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

    public String translateToLocale(UUID i18nId, Locale locale, Map<String, String> context) {
        String ret = null;
        if (locale == null)
            locale = i18nProperties.defaultLocale();
        if (i18nId == null) {
            log.warn("I18n not configured");
            return "";
        }
        Optional<I18nTranslationEntity> i18nTranslationEntity = i18nTranslationRepository.findByI18nIdAndLocale(i18nId, locale);
        if (i18nTranslationEntity.isPresent())
            ret = i18nTranslationEntity.get().getTranslation();
        if (StringUtils.isBlank(ret)) {
            i18nTranslationRepository.incrementUsageCounter(i18nId, locale.getLanguage());
            i18nTranslationEntity = i18nTranslationRepository.findByI18nIdAndLocale(i18nId, i18nProperties.defaultLocale());
            if (i18nTranslationEntity.isPresent())
                ret = i18nTranslationEntity.get().getTranslation();
        }
        if (MapUtils.isNotEmpty(context))
            return StringUtils.replaceVariables(ret, context);
        else
            return ret;
    }

    public I18nTranslationEntity getTranslationEntity(I18nEntity i18NEntity, Locale locale) {
        return getTranslationEntity(i18NEntity, locale, null);
    }

    public I18nTranslationEntity getTranslationEntity(I18nEntity i18NEntity, Locale locale, Map<String, String> context) {
        I18nTranslationEntity ret = null;
        if (locale == null)
            locale = i18nProperties.defaultLocale();
        Optional<I18nTranslationEntity> i18nTranslationEntity = i18nTranslationRepository.findByI18nIdAndLocale(i18NEntity.getId(), locale);
        if (i18nTranslationEntity.isEmpty() || StringUtils.isBlank(i18nTranslationEntity.get().getTranslation())) {
            i18nTranslationRepository.incrementUsageCounter(i18NEntity.getId(), locale.getLanguage());
            i18nTranslationEntity = i18nTranslationRepository.findByI18nIdAndLocale(i18NEntity.getId(), i18nProperties.defaultLocale());
        }
        if (i18nTranslationEntity.isEmpty())
            return null;
        loadStyles(i18nTranslationEntity.get());
        return i18nTranslationEntity.get();
    }

    public String translateBase24ToLocale(I18nEntity i18NEntity, Locale locale) {
        Optional<I18nTranslationBinEntity> translationBinEntity = i18nTranslationBinRepository.findByI18nAndLocale(i18NEntity, locale);
        if (translationBinEntity.isEmpty() || translationBinEntity.get().getTranslation().length == 0)
            translationBinEntity = i18nTranslationBinRepository.findByI18nAndLocale(i18NEntity, i18nProperties.defaultLocale());
        if (translationBinEntity.isPresent() && translationBinEntity.get().getTranslation().length > 0)
            return translationBinEntity.get().getBase64();
        return null;
    }


    public String translateToLocale(String i18nKey, Locale locale) throws ServiceException {
        I18nEntity i18NEntity = i18nRepository.findByKey(i18nKey);
        if (i18NEntity == null)
            throw new ServiceException(ErrorCodeI18n.INCORRECT_CONFIGURATION, "Wrong i18n key: " + i18nKey);
        return translateToLocale(i18NEntity, locale, null);
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
        List<Locale> locales = Arrays.asList(i18nProperties.defaultLocale(), Locale.forLanguageTag(locale));
        if (i18nEntity.getType().isImage()) {
            i18nEntity.setTranslationsBin(i18nTranslationBinRepository.findByI18nAndLocaleIn(i18nEntity, locales));
        } else {
            i18nEntity.setTranslations(i18nTranslationRepository.findByI18nAndLocaleIn(i18nEntity, locales));
            if (i18nEntity.getType().isStyledText()) {
                for (I18nTranslationEntity translation : i18nEntity.getTranslations()) {
                    translation.setStyles(i18nTranslationStyleRepository.findByI18nAndLocale(translation.getI18n(), translation.getLocale()));
                }
            }
        }
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
        try {
            return translateToLocale(i18NEntity, resolveCurrentUserLocale(), context);
        } catch (Exception e) {
            return "";
        }
    }

    public String translateToLocale(UUID i18nId, Map<String, String> context) {
        try {
            return translateToLocale(i18nId, resolveCurrentUserLocale(), context);
        } catch (Exception e) {
            return "";
        }
    }

    public String translateToLocale(I18nEntity i18NEntity) {
        try {
            return translateToLocale(i18NEntity, resolveCurrentUserLocale(), null);
        } catch (Exception e) {
            return "";
        }
    }

    public String translateToLocale(UUID i18nId) {
        try {
            return translateToLocale(i18nId, resolveCurrentUserLocale(), null);
        } catch (Exception e) {
            return "";
        }
    }

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

    private String addCopyPostfix(String originalStr) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(originalStr))
            return originalStr;
        return originalStr + " [copy]";
    }

    protected abstract Locale resolveCurrentUserLocale() throws ServiceException;
}
