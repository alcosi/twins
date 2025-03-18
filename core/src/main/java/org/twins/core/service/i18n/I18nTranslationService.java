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
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.service.domain.DomainService;
import org.twins.core.domain.i18n.I18nTranslation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class I18nTranslationService extends EntitySecureFindServiceImpl<I18nTranslationEntity> {
    private final I18nTranslationRepository repository;
    private final I18nService i18nService;
    private final DomainService domainService;

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
                if (entity.getI18n().getDomain() == null || !entity.getI18n().getDomain().getId().equals(entity.getI18n().getDomainId()))
                    entity.getI18n().setDomain(domainService.findEntitySafe(entity.getI18n().getDomainId()));
        }
        return true;
    }

    @Transactional
    public List<I18nTranslationEntity> updateI18nTranslations(List<I18nTranslation> updates) throws ServiceException {
        UUID i18nId = updates.getFirst().getI18nId();
        List<I18nTranslationEntity> dbEntities = repository.findByI18nId(i18nId);
        ChangesHelper changesHelper = new ChangesHelper();

        Map<Locale, I18nTranslationEntity> dbEntitiesMap = dbEntities.stream()
                .collect(Collectors.toMap(I18nTranslationEntity::getLocale, Function.identity()));

        List<I18nTranslationEntity> updatedEntities = new ArrayList<>();

        for (I18nTranslation update : updates) {
            for (Map.Entry<Locale, String> entry : update.getEntities().entrySet()) {
                Locale locale = entry.getKey();
                String newTranslation = entry.getValue();

                I18nTranslationEntity dbEntity = dbEntitiesMap.get(locale);
                if (dbEntity == null) {
                    dbEntity = new I18nTranslationEntity()
                            .setI18nId(i18nId)
                            .setLocale(locale);
                    dbEntitiesMap.put(locale, dbEntity);
                }

                I18nTranslationEntity updateEntity = new I18nTranslationEntity()
                        .setTranslation(newTranslation);

                updateEntityField(
                        updateEntity,
                        dbEntity,
                        I18nTranslationEntity::getTranslation,
                        I18nTranslationEntity::setTranslation,
                        I18nTranslationEntity.Fields.translation,
                        changesHelper
                );

                if (changesHelper.hasChanges()) {
                    validateEntity(dbEntity, EntitySmartService.EntityValidateMode.beforeSave);
                }

                updatedEntities.add(dbEntity);
            }
        }

        return StreamSupport.stream(repository.saveAll(updatedEntities).spliterator(), false)
                .collect(Collectors.toList());
    }
}
