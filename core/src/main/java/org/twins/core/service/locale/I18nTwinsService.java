package org.twins.core.service.locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.service.i18n.I18nService;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
@Slf4j
public class I18nTwinsService extends I18nService {

    final AuthService authService;
    @Override
    protected Locale resolveCurrentUserLocale() {
        ApiUser apiUser;
        try {
            apiUser = authService.getApiUser();
            return apiUser.getLocale();
        } catch (ServiceException e) {
            return null;
        }
    }

    @Override
    protected Locale resolveDefaultLocale() {
        ApiUser apiUser;
        try {
            apiUser = authService.getApiUser();
            Locale domainLocale = null;
            if(apiUser.isDomainSpecified())
                domainLocale = apiUser.getDomain().getDefaultI18nLocaleId();
            return domainLocale != null ? domainLocale : super.resolveDefaultLocale();
        } catch (ServiceException e) {
            return super.resolveDefaultLocale();
        }
    }

    @Override
    public CrudRepository<I18nEntity, UUID> entityRepository() {
        return null;
    }

    @Override
    public Function<I18nEntity, UUID> entityGetIdFunction() {
        return null;
    }

    @Override
    public boolean isEntityReadDenied(I18nEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(I18nEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
