package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainLocaleEntity;
import org.twins.core.dao.domain.DomainLocaleRepository;
import org.twins.core.dao.i18n.I18nLocaleEntity;
import org.twins.core.dao.i18n.I18nLocaleRepository;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class DomainLocaleService extends EntitySecureFindServiceImpl<DomainLocaleEntity> {
    private final DomainLocaleRepository domainLocaleRepository;
    private final I18nLocaleRepository i18nLocaleRepository;
    @Override
    public CrudRepository<DomainLocaleEntity, UUID> entityRepository() {
        return domainLocaleRepository;
    }

    @Override
    public Function<DomainLocaleEntity, UUID> entityGetIdFunction() {
        return DomainLocaleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainLocaleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(DomainLocaleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void addDomainLocale(UUID domainId, Locale locale) throws ServiceException {
        I18nLocaleEntity localeEntity = i18nLocaleRepository.getByLocale(locale.toLanguageTag());
        if (localeEntity == null || !localeEntity.isActive())
            throw new ServiceException(ErrorCodeTwins.DOMAIN_LOCALE_INACTIVE, "locale is not active in system");
        DomainLocaleEntity domainLocaleEntity = new DomainLocaleEntity()
                .setDomainId(domainId)
                .setLocale(locale)
                .setActive(true)
                .setI18nLocale(localeEntity);
        saveSafe(domainLocaleEntity);
    }
}
