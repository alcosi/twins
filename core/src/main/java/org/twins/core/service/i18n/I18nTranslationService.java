package org.twins.core.service.i18n;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.domain.i18n.I18nSave;
import org.twins.core.service.domain.DomainService;

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
    public List<I18nTranslationEntity> updateI18nTranslations(List<I18nSave> updates) throws ServiceException {
        return updateTranslations(convertToList(updates));
    }

    @Transactional
    public List<I18nTranslationEntity> updateTranslations(List<I18nTranslationEntity> updates) throws ServiceException {
        KitGrouped<I18nTranslationEntity, String, UUID> translationUpdateKit = new KitGrouped<>(updates, I18nTranslationEntity::getKitKey, I18nTranslationEntity::getI18nId);
        List<I18nTranslationEntity> dbEntities = repository.findByI18nIdIn(translationUpdateKit.getGroupedMap().keySet());
        KitGrouped<I18nTranslationEntity, String, UUID> translationDbKit = new KitGrouped<>(dbEntities, I18nTranslationEntity::getKitKey, I18nTranslationEntity::getI18nId);

        List<I18nTranslationEntity> updatedEntities = new ArrayList<>();
        for (var entry : translationUpdateKit.getMap().entrySet()) {
            I18nTranslationEntity updateTranslationEntity = entry.getValue();
            I18nTranslationEntity dbTranslationEntity = KitUtils.isEmpty(translationDbKit) ? null : translationDbKit.getMap().get(entry.getKey());
            if (dbTranslationEntity == null) {
                // adding new translation
                updatedEntities.add(entry.getValue());
            } else if (updateTranslationEntity.getTranslation() != null && !updateTranslationEntity.getTranslation().equals(dbTranslationEntity.getTranslation())) {
                dbTranslationEntity.setTranslation(updateTranslationEntity.getTranslation());
                updatedEntities.add(dbTranslationEntity);
            }
        }
        if (!updatedEntities.isEmpty()) {
            return StreamSupport.stream(repository.saveAll(updatedEntities).spliterator(), false)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @NotNull
    private static List<I18nTranslationEntity> convertToList(List<I18nSave> updates) {
        List<I18nTranslationEntity> ret = new ArrayList<>();
        for (var update : updates) {
            for (Map.Entry<Locale, String> entry : update.getTranslations().entrySet()) {
                ret.add(new I18nTranslationEntity()
                        .setI18nId(update.i18nId)
                        .setLocale(entry.getKey())
                        .setTranslation(entry.getValue()));
            }
        }
        return ret;
    }
}
