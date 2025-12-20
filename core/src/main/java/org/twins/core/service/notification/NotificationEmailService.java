package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.notification.email.NotificationEmailEntity;
import org.twins.core.dao.notification.email.NotificationEmailRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.email.EmailSenderService;
import org.twins.core.service.event.Event;
import org.twins.core.service.event.context.ContextLoaderDomainData;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.template.TemplateGeneratorService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class NotificationEmailService extends EntitySecureFindServiceImpl<NotificationEmailEntity> {
    private final NotificationEmailRepository notificationEmailRepository;
    private final TemplateGeneratorService templateGeneratorService;
    private final I18nService i18nService;
    @Lazy
    private final AuthService authService;
    private final EmailSenderService emailSenderService;

    @Override
    public CrudRepository<NotificationEmailEntity, UUID> entityRepository() {
        return notificationEmailRepository;
    }

    @Override
    public Function<NotificationEmailEntity, UUID> entityGetIdFunction() {
        return NotificationEmailEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(NotificationEmailEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null && !entity.getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(NotificationEmailEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void notify(Event event, String emailDst) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        NotificationEmailEntity notificationEmailEntity = notificationEmailRepository.findByDomainIdAndEventId(apiUser.getDomainId(), event.getEvent().getId());
        if (notificationEmailEntity == null) {
            log.error("Not notification[{}] configured for {}", event.getEvent().getId(), apiUser.getDomain().logShort());
            return;
        } else if (isEntityReadDenied(notificationEmailEntity)) {
            return;
        } else if (!notificationEmailEntity.isActive()) {
            log.error("{} is inactive", event.getEvent().getId());
            return;
        }
        ContextLoaderDomainData.INSTANCE.load(event, authService.getApiUser().getDomain());
        String subject = null, body = null;
        if (notificationEmailEntity.getSubjectTemplateGeneratorId() != null) {
            subject = templateGeneratorService.generate(notificationEmailEntity.getSubjectTemplateGenerator(), notificationEmailEntity.getSubjectI18nId(), event.getContext());
        } else {
            subject = i18nService.translateToLocale(notificationEmailEntity.getSubjectI18nId());
        }
        if (StringUtils.isEmpty(subject)) {
            throw new ServiceException(ErrorCodeTwins.NOTIFICATION_CONFIGURATION_ERROR, "email subject is empty");
        }
        if (notificationEmailEntity.getBodyTemplateGeneratorId() != null) {
            body = templateGeneratorService.generate(notificationEmailEntity.getBodyTemplateGenerator(), notificationEmailEntity.getBodyI18nId(), event.getContext());
        } else {
            body = i18nService.translateToLocale(notificationEmailEntity.getBodyI18nId());
        }
        if (StringUtils.isEmpty(body)) {
            throw new ServiceException(ErrorCodeTwins.NOTIFICATION_CONFIGURATION_ERROR, "email body is empty");
        }
        emailSenderService.sendEmail(
                notificationEmailEntity.getEmailSender(),
                emailDst,
                subject,
                body, notificationEmailEntity.getNotificationMode());
    }
}
