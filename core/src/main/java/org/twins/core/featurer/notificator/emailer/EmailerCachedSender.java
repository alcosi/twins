package org.twins.core.featurer.notificator.emailer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class EmailerCachedSender<S> extends Emailer {
//    @Override
//    protected void sendMail(UUID emailSenderId, Properties properties, String dstEmail, String subject, String text) throws ServiceException {
//        S sender = getSender(emailSenderId, properties);
//        sendMail(sender, properties, dstEmail, subject, text);
//    }
//
//    protected abstract void sendMail(S sender, Properties properties, String dstEmail, String subject, String text) throws ServiceException;

    @Override
    protected void sendMail(UUID emailSenderId, Properties properties, String srcEmail, String dstEmail, String subjectOrTemplateId, String bodyOrTemplateId, Map<String, String> templateVars) throws ServiceException {
        S sender = getSender(emailSenderId, properties);
        sendMail(sender, properties, dstEmail, srcEmail, subjectOrTemplateId, bodyOrTemplateId, templateVars);
    }

    protected abstract void sendMail(S sender, Properties properties, String dstEmail, String srcEmail, String subject, String body, Map<String, String> templateVars) throws ServiceException;

    public S createSender(HashMap<String, String> emailerParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, emailerParams);
        return createSender(properties);
    }

    protected abstract S createSender(Properties properties) throws ServiceException;

    private final Map<UUID, S> senderCache = new ConcurrentHashMap<>();

    public S getSender(UUID mailerId, Properties properties) throws ServiceException {
        if (!senderCache.containsKey(mailerId)) {
            S sender = createSender(properties);
            senderCache.put(mailerId, sender);
        }
        return senderCache.get(mailerId);
    }
}
