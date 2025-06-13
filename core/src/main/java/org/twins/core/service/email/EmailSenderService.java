package org.twins.core.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.email.EmailSenderEntity;
import org.twins.core.dao.email.EmailSenderRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.notificator.emailer.Emailer;
import org.twins.core.service.auth.AuthService;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class EmailSenderService extends EntitySecureFindServiceImpl<EmailSenderEntity> {
    private final EmailSenderRepository emailSenderRepository;
    @Lazy
    private final AuthService authService;
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<EmailSenderEntity, UUID> entityRepository() {
        return emailSenderRepository;
    }

    @Override
    public Function<EmailSenderEntity, UUID> entityGetIdFunction() {
        return EmailSenderEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(EmailSenderEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null && !entity.getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(EmailSenderEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void sendEmail(EmailSenderEntity emailSender, String dstEmail, String subject, String body) throws ServiceException {
        sendEmail(emailSender,  dstEmail, subject, body, null);
    }

    public void sendEmail(EmailSenderEntity emailSender, String dstEmail, String subject, String body, Map<String, String> templateVars) throws ServiceException {
        Emailer emailer = featurerService.getFeaturer(emailSender.getEmailerFeaturerId(), Emailer.class);
        emailer.sendMail(emailSender.getId(), emailSender.getEmailerParams(), emailSender.getSrcEmail(), dstEmail, subject, body, templateVars);
    }

}
