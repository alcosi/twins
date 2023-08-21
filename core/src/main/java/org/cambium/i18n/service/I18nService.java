package org.cambium.i18n.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.config.I18nProperties;
import org.cambium.i18n.dao.*;
import org.cambium.common.util.StringUtils;
import org.cambium.i18n.exception.ErrorCodeI18n;
import org.springframework.stereotype.Component;
import org.twins.core.service.HttpRequestService;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class I18nService {
    final I18nRepository i18nRepository;
    final I18nTranslationRepository i18nTranslationRepository;
    final I18nTranslationBinRepository i18nTranslationBinRepository;
    final I18nTranslationStyleRepository i18nTranslationStyleRepository;
    final I18nLocaleRepository i18nLocaleRepository;
    final I18nProperties i18nProperties;
    final HttpRequestService httpRequestService;
    ;

    public String translateToLocale(I18nEntity i18NEntity, Locale locale) {
        return translateToLocale(i18NEntity, locale, null);
    }

    public String translateToLocaleOrEmpty(I18nEntity i18NEntity, Locale locale) {
        if (i18NEntity == null)
            return "";
        return translateToLocale(i18NEntity, locale, null);
    }

    public String translateToLocale(I18nEntity i18NEntity, Locale locale, Map<String, String> context) {
        String ret = null;
        if (locale == null)
            locale = i18nProperties.defaultLocale();
        Optional<I18nTranslationEntity> i18nTranslationEntity = i18nTranslationRepository.findByI18nAndLocale(i18NEntity, locale);
        if (i18nTranslationEntity.isPresent())
            ret = i18nTranslationEntity.get().getTranslation();
        if (StringUtils.isBlank(ret)) {
            i18nTranslationRepository.incrementUsageCounter(i18NEntity.getId(), locale.getLanguage());
            i18nTranslationEntity = i18nTranslationRepository.findByI18nAndLocale(i18NEntity, i18nProperties.defaultLocale());
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
        Optional<I18nTranslationEntity> i18nTranslationEntity = i18nTranslationRepository.findByI18nAndLocale(i18NEntity, locale);
        if (i18nTranslationEntity.isEmpty() || StringUtils.isBlank(i18nTranslationEntity.get().getTranslation())) {
            i18nTranslationRepository.incrementUsageCounter(i18NEntity.getId(), locale.getLanguage());
            i18nTranslationEntity = i18nTranslationRepository.findByI18nAndLocale(i18NEntity, i18nProperties.defaultLocale());
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
        return translateToLocale(i18NEntity, httpRequestService.getLocale(), context);
    }

    public String translateToLocale(I18nEntity i18NEntity) {
        return translateToLocale(i18NEntity, httpRequestService.getLocale(), null);
    }
}
