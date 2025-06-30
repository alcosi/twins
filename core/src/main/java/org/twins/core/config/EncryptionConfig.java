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
    private static String featurerParamCryptAlgorithm = "PBEWithMD5AndDES";
    private static String featurerParamCryptPassword;

    @Value("${twin.field.password.key}")
    private String secretKey;

    @Value("${twin.field.password.algorithm:PBEWithMD5AndDES}")
    private String cryptoAlgorithm = "PBEWithMD5AndDES";

    @Bean
    public StandardPBEStringEncryptor secretEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(secretKey);
        encryptor.setAlgorithm(cryptoAlgorithm);
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor;
    }

    public static StandardPBEStringEncryptor getEncryptor() {
        if (encryptor != null) return encryptor;
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(featurerParamCryptPassword);
        encryptor.setAlgorithm(featurerParamCryptAlgorithm);
        return encryptor;
    }

    @Value("${featurer.param.encrypt.key}")
    public void setKey(String key) {
        if (EncryptionConfig.featurerParamCryptPassword != null) return;
        EncryptionConfig.featurerParamCryptPassword = key;
    }

    @Value("${featurer.param.encrypt.algorithm:PBEWithMD5AndDES}")
    public void setAlgorithm(String algorithm) {
        if (EncryptionConfig.featurerParamCryptAlgorithm != null) return;
        EncryptionConfig.featurerParamCryptAlgorithm = algorithm;
    }


    @EventListener
    private void initializeConfigurableStaticFields(ContextRefreshedEvent event) {
    }
}
