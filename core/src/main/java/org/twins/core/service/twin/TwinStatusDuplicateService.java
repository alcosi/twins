package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.KeyUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.twinstatus.TwinStatusDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatusDuplicateService extends EntityDuplicateService<TwinStatusDuplicate, TwinStatusEntity> {

    @Lazy
    private final TwinStatusService twinStatusService;
    @Lazy
    private final TwinClassService twinClassService;
    private final I18nService i18nService;

    @Override
    protected EntitySecureFindServiceImpl<TwinStatusEntity> entityService() {
        return twinStatusService;
    }

    @Override
    protected org.cambium.common.exception.ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT;
    }

    @Override
    protected void prepareDuplicates(Collection<TwinStatusDuplicate> duplicates) throws ServiceException {
        for (var duplicate : duplicates) {
            if (duplicate.getNewTwinClassId() == null) {
                TwinStatusEntity original = duplicate.getOriginalEntity();
                duplicate
                        .setNewTwinClassId(original.getTwinClassId())
                        .setNewTwinClass(original.getTwinClass());
            }
        }
        twinClassService.load(duplicates,
                TwinStatusDuplicate::getNewTwinClassId,
                TwinStatusDuplicate::getNewTwinClass,
                TwinStatusDuplicate::setNewTwinClass);
    }

    @Override
    protected TwinStatusEntity createNewEntity(TwinStatusDuplicate duplicate, TwinStatusEntity original) throws ServiceException {
        TwinClassEntity targetClass = duplicate.getNewTwinClass();
        log.info("{} will be duplicated for {}", original.logNormal(), targetClass.logNormal());
        return new TwinStatusEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(duplicate.getNewKey(), ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT))
                .setTwinClassId(targetClass.getId())
                .setTwinClass(targetClass)
                .setInheritable(original.getInheritable())
                .setBackgroundColor(original.getBackgroundColor())
                .setFontColor(original.getFontColor())
                .setType(original.getType());
    }

    @Override
    protected void duplicateI18nFields(TwinStatusEntity src, TwinStatusEntity dst) {
        if (src.getNameI18nId() != null) {
            dst.setNameI18nId(i18nService.duplicateI18n(src.getNameI18nId()).getId());
        }
        if (src.getDescriptionI18nId() != null) {
            dst.setDescriptionI18nId(i18nService.duplicateI18n(src.getDescriptionI18nId()).getId());
        }
    }
}
