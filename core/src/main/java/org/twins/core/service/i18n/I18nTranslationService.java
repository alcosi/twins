package org.twins.core.service.i18n;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class I18nTranslationService extends EntitySecureFindServiceImpl<I18nTranslationEntity> {
    private final I18nTranslationRepository repository;
    private final I18nService i18nService;

    @Override
    public CrudRepository<I18nTranslationEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<I18nTranslationEntity, UUID> entityGetIdFunction() {
        return I18nTranslationEntity::getI18nId;
    }

    @Override
    public boolean isEntityReadDenied(I18nTranslationEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(I18nTranslationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getI18n() == null || !entity.getI18n().getId().equals(entity.getI18nId()))
                    entity.setI18n(i18nService.findEntitySafe(entity.getI18nId()));
        }
        return true;
    }

    public List<I18nTranslationEntity> updateI18nTranslations(UUID i18nId, List<I18nTranslationEntity> entities) throws ServiceException {
        List<I18nTranslationEntity> updatedEntities = new ArrayList<>();

        for (Map.Entry<Locale, String> entry : translations.entrySet()) {
            Locale locale = entry.getKey();
            String translation = entry.getValue();

            I18nTranslationEntity entity = repository.findByI18nIdAndLocale(i18nId, locale)
                    .orElse(new I18nTranslationEntity().setI18nId(i18nId).setLocale(locale));

            entity.setTranslation(translation);

            validateEntity(entity, EntitySmartService.EntityValidateMode.beforeSave);
            updatedEntities.add(repository.save(entity));
        }

        return updatedEntities;
    }
}
