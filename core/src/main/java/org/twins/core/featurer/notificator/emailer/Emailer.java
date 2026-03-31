package org.twins.core.featurer.notificator.emailer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@FeaturerType(id = FeaturerTwins.TYPE_33,
        name = "Emailer",
        description = "Send email")
@Slf4j
public abstract class Emailer extends FeaturerTwins {
//    public void sendMail(UUID emailSenderId, HashMap<String, String> emailerParams, String srcEmail, String dstEmail, String subject, String body) throws ServiceException {
//        Properties properties = featurerService.extractProperties(this, emailerParams, new HashMap<>());
//        sendMail(emailSenderId, properties, dstEmail, subject, body);
//    }
//
//    protected abstract void sendMail(UUID emailSenderId, Properties properties, String dstEmail, String subject, String text) throws ServiceException;

    public void sendMail(UUID emailSenderId, HashMap<String, String> emailerParams, String srcEmail, String dstEmail, String subject, String templateId, Map<String, String> templateVars) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, emailerParams);
        sendMail(emailSenderId, properties, srcEmail, dstEmail, subject, templateId, templateVars);
    }

    protected abstract void sendMail(UUID emailSenderId, Properties properties, String srcEmail, String dstEmail, String subjectOrTemplateId, String bodyOrTemplateId, Map<String, String> templateVars) throws ServiceException;

}
