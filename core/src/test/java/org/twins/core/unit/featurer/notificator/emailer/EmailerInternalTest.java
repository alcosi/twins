package org.twins.core.featurer.notificator.emailer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.twins.core.base.BaseUnitTest;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailerInternalTest extends BaseUnitTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailerInternal emailer;

    @BeforeEach
    void setUp() throws Exception {
        emailer = new EmailerInternal();
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try { return clazz.getDeclaredField(fieldName); }
            catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private Properties buildEmailerProperties() {
        var props = new Properties();
        props.setProperty("host", "smtp.example.com");
        props.setProperty("port", "587");
        props.setProperty("username", "user@example.com");
        props.setProperty("password", "secret123");
        props.setProperty("auth", "true");
        props.setProperty("starttls", "true");
        return props;
    }

    @Nested
    class CreateSender {

        @Test
        void createSender_configuresAllProperties() throws Exception {
            var props = buildEmailerProperties();

            var sender = emailer.createSender(props);

            assertInstanceOf(JavaMailSenderImpl.class, sender);
            var impl = (JavaMailSenderImpl) sender;
            assertEquals("smtp.example.com", impl.getHost());
            assertEquals(587, impl.getPort());
            assertEquals("user@example.com", impl.getUsername());
            assertEquals("secret123", impl.getPassword());
            assertEquals("true", impl.getJavaMailProperties().getProperty("mail.smtp.auth"));
            assertEquals("true", impl.getJavaMailProperties().getProperty("mail.smtp.starttls.enable"));
        }
    }

    @Nested
    class SendMail {

        @Test
        void sendMail_createsMessageAndSends() throws Exception {
            var sender = mock(JavaMailSender.class);
            var props = new Properties();
            var templateVars = Map.of("key1", "value1");

            emailer.sendMail(sender, props, "to@test.com", "from@test.com",
                    "Test Subject", "Test Body", templateVars);

            verify(sender).send(any(SimpleMailMessage.class));
        }

        @Test
        void sendMail_setsCorrectMessageFields() throws Exception {
            var sender = mock(JavaMailSender.class);
            var props = new Properties();
            var templateVars = Map.of("key1", "value1");
            var capturedMessage = new SimpleMailMessage[] { null };

            doAnswer(invocation -> {
                capturedMessage[0] = invocation.getArgument(0);
                return null;
            }).when(sender).send(any(SimpleMailMessage.class));

            emailer.sendMail(sender, props, "to@test.com", "from@test.com",
                    "Test Subject", "Test Body", templateVars);

            assertNotNull(capturedMessage[0]);
            assertEquals("to@test.com", capturedMessage[0].getTo()[0]);
            assertEquals("from@test.com", capturedMessage[0].getFrom());
            assertEquals("Test Subject", capturedMessage[0].getSubject());
            assertEquals("Test Body", capturedMessage[0].getText());
        }
    }
}
