package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.domain.factory.FactoryDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryDuplicateService extends EntityDuplicateService<FactoryDuplicate, TwinFactoryEntity> {

    @Lazy
    private final TwinFactoryService twinFactoryService;
    @Lazy
    private final FactoryBranchDuplicateService factoryBranchDuplicateService;
    @Lazy
    private final FactoryMultiplierDuplicateService factoryMultiplierDuplicateService;
    @Lazy
    private final FactoryPipelineDuplicateService factoryPipelineDuplicateService;
    @Lazy
    private final FactoryEraserDuplicateService factoryEraserDuplicateService;
    @Lazy
    private final FactoryTriggerDuplicateService factoryTriggerDuplicateService;
    private final I18nService i18nService;
    @Lazy
    private final AuthService authService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEntity> entityService() {
        return twinFactoryService;
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void prepareDuplicates(Collection<FactoryDuplicate> duplicates) throws ServiceException {
        var apiUser = authService.getApiUser();
        for (var duplicate : duplicates) {
            duplicate.setNewFactoryId(UUID.nameUUIDFromBytes((duplicate.getNewKey() + apiUser.getDomainId()).getBytes()));
        }
    }

    @Override
    protected TwinFactoryEntity createNewEntity(FactoryDuplicate duplicate) throws ServiceException {
        var original = duplicate.getOriginalEntity();
        var apiUser = authService.getApiUser();
        log.info("{} will be duplicated with new key[{}]", original.logShort(), duplicate.getNewKey());
        return new TwinFactoryEntity()
                .setKey(duplicate.getNewKey())
                .setCreatedByUserId(apiUser.getUser().getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setDomainId(original.getDomainId());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryEntity src, TwinFactoryEntity dst) throws ServiceException {
        if (src.getNameI18NId() != null) {
            I18nEntity i18nDuplicate = i18nService.duplicateI18n(src.getNameI18NId());
            dst.setNameI18NId(i18nDuplicate.getId());
        }
        if (src.getDescriptionI18NId() != null) {
            I18nEntity i18nDuplicate = i18nService.duplicateI18n(src.getDescriptionI18NId());
            dst.setDescriptionI18NId(i18nDuplicate.getId());
        }
    }

    @Override
    protected void afterSave(Collection<FactoryDuplicate> duplicates, Collection<TwinFactoryEntity> saved) throws ServiceException {
        for (var duplicate : duplicates) {
            twinFactoryService.loadFactoryElements(duplicate.getOriginalEntity());
            if (duplicate.isDuplicateBranches()) {
                factoryBranchDuplicateService.duplicateForFactory(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
            if (duplicate.isDuplicateMultipliers()) {
                factoryMultiplierDuplicateService.duplicateForFactory(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
            if (duplicate.isDuplicatePipelines()) {
                factoryPipelineDuplicateService.duplicateForFactory(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
            if (duplicate.isDuplicateErasers()) {
                factoryEraserDuplicateService.duplicateForFactory(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
            if (duplicate.isDuplicateTriggers()) {
                factoryTriggerDuplicateService.duplicateForFactory(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
        }
    }
}
