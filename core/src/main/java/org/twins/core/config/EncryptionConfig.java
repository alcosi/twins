package org.twins.core.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class EncryptionConfig {

    private static StandardPBEStringEncryptor encryptor;
    private static String password;

    @Value("${twin.field.password.key:secret}")
    private String secretKey;

    @Bean
    public StandardPBEStringEncryptor secretEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(secretKey);
        encryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor;
    }

    public static StandardPBEStringEncryptor getEncryptor() {
        if (encryptor != null) return encryptor;
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
        return encryptor;
    }

    @Value("${featurer.param.encrypt.key:secret}")
    public void setKey(String key) {
        if (EncryptionConfig.password != null) return;
        EncryptionConfig.password = key;
    }

    @EventListener
    private void initializeConfigurableStaticFields(ContextRefreshedEvent event) {
    }
}
