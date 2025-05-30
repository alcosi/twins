package org.twins.core.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class EncryptionConfig {

    private static StandardPBEStringEncryptor encryptor;
    private static String password;

    public static StandardPBEStringEncryptor getEncryptor() {
        if (encryptor != null) return encryptor;
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
        return encryptor;
    }

    @Value("${featurer.param.encrypt.key}")
    public void setKey(String key) {
        if (EncryptionConfig.password != null) return;
        EncryptionConfig.password = key;
    }

    @EventListener
    private void initializeConfigurableStaticFields(ContextRefreshedEvent event) {
    }
}
