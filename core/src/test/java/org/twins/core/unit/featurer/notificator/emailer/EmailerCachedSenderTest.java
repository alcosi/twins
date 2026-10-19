package org.twins.core.featurer.notificator.emailer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmailerCachedSenderTest extends BaseUnitTest {

    private TestableEmailerCachedSender emailer;

    @BeforeEach
    void setUp() {
        emailer = new TestableEmailerCachedSender();
    }

    /**
     * Concrete test subclass that captures sendMail calls.
     */
    static class TestableEmailerCachedSender extends EmailerCachedSender<String> {

        String lastSender;
        Properties lastProperties;
        String lastDstEmail;
        String lastSrcEmail;
        String lastSubject;
        String lastBody;
        Map<String, String> lastTemplateVars;
        int sendMailCallCount;
        String createdSender;

        @Override
        protected void sendMail(String sender, Properties properties, String dstEmail,
                                String srcEmail, String subject, String body,
                                Map<String, String> templateVars) {
            lastSender = sender;
            lastProperties = properties;
            lastDstEmail = dstEmail;
            lastSrcEmail = srcEmail;
            lastSubject = subject;
            lastBody = body;
            lastTemplateVars = templateVars;
            sendMailCallCount++;
        }

        @Override
        protected String createSender(Properties properties) {
            createdSender = "sender-" + properties.getProperty("key", "default");
            return createdSender;
        }
    }

    @Nested
    class GetSender {

        @Test
        void getSender_firstCall_createsNewSender() throws Exception {
            var props = new Properties();
            props.setProperty("key", "value1");

            var sender = emailer.getSender(UUID.randomUUID(), props);

            assertEquals("sender-value1", sender);
            assertEquals("sender-value1", emailer.createdSender);
        }

        @Test
        void getSender_sameId_returnsCachedSender() throws Exception {
            var mailerId = UUID.randomUUID();
            var props1 = new Properties();
            props1.setProperty("key", "value1");
            var props2 = new Properties();
            props2.setProperty("key", "value2");

            var sender1 = emailer.getSender(mailerId, props1);
            var sender2 = emailer.getSender(mailerId, props2);

            assertSame(sender1, sender2);
            assertEquals("sender-value1", sender2);
        }

        @Test
        void getSender_differentIds_createsSeparateSenders() throws Exception {
            var props1 = new Properties();
            props1.setProperty("key", "value1");
            var props2 = new Properties();
            props2.setProperty("key", "value2");

            var sender1 = emailer.getSender(UUID.randomUUID(), props1);
            var sender2 = emailer.getSender(UUID.randomUUID(), props2);

            assertEquals("sender-value1", sender1);
            assertEquals("sender-value2", sender2);
        }
    }

    @Nested
    class SendMail {

        @Test
        void sendMail_delegatesToProtectedMethodWithSender() throws Exception {
            var props = new Properties();
            props.setProperty("key", "test");
            var emailSenderId = UUID.randomUUID();
            var templateVars = new HashMap<String, String>();
            templateVars.put("var1", "value1");

            emailer.sendMail(emailSenderId, props, "from@test.com", "to@test.com",
                    "Subject", "Body", templateVars);

            assertEquals(1, emailer.sendMailCallCount);
            assertEquals("sender-test", emailer.lastSender);
            assertEquals("to@test.com", emailer.lastDstEmail);
            assertEquals("from@test.com", emailer.lastSrcEmail);
            assertEquals("Subject", emailer.lastSubject);
            assertEquals("Body", emailer.lastBody);
            assertEquals(templateVars, emailer.lastTemplateVars);
        }
    }
}
