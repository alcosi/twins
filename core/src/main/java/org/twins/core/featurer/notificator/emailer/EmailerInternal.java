package org.twins.core.featurer.notificator.emailer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamEncrypted;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3301,
        name = "Internal",
        description = "")
public class EmailerInternal extends EmailerCachedSender<JavaMailSender> {
    @FeaturerParam(name = "Host", description = "SMTP server ip", order = 1)
    public static final FeaturerParamString host = new FeaturerParamString("host");

    @FeaturerParam(name = "Port", description = "SMTP server port", order = 2)
    public static final FeaturerParamInt port = new FeaturerParamInt("port");

    @FeaturerParam(name = "Username", description = "Username", order = 3)
    public static final FeaturerParamString username = new FeaturerParamString("username");

    @FeaturerParam(name = "Password", description = "Password", order = 4)
    public static final FeaturerParamEncrypted password = new FeaturerParamEncrypted("password");

    @FeaturerParam(name = "Auth", description = "Is smtp auth enabled", order = 5, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean auth = new FeaturerParamBoolean("auth");

    @FeaturerParam(name = "Starttls", description = "Is STARTTLS enabled", order = 6, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean starttls = new FeaturerParamBoolean("starttls");

    @Override
    protected void sendMail(JavaMailSender sender, Properties properties, String dstEmail, String srcEmail, String subject, String body, Map<String, String> templateVars) throws ServiceException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dstEmail);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(srcEmail);
        sender.send(message);
    }

    @Override
    protected JavaMailSenderImpl createSender(Properties properties) throws ServiceException {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host.extract(properties));
        sender.setPort(port.extract(properties));
        sender.setUsername(username.extract(properties));
        sender.setPassword(password.extract(properties));

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(auth.extract(properties)));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttls.extract(properties)));
        return sender;
    }
}
