package org.twins.core.service.i18n;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
                i18nService.validateEntity(entity.getI18n(), entityValidateMode);
        }
        return true;
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

}
