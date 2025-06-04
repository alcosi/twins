package org.cambium.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;

import java.util.Properties;

import static org.twins.core.config.EncryptionConfig.getEncryptor;

@FeaturerParamType(
        id = "ENCRYPTED_STRING",
        description = "encrypted string",
        regexp = ".*",
        example = "")
public class FeaturerParamEncrypted extends FeaturerParam<String> {

    private static final String ENCRYPTED_VALUE_PREFIX = "encrypted_";

    public FeaturerParamEncrypted(String key) {
        super(key);
    }

    @Override
    public String extract(Properties properties) {
        String value = (String) properties.get(key);
        if (!value.startsWith(ENCRYPTED_VALUE_PREFIX)) return value;
        String valueWithoutPrefix = value.substring(ENCRYPTED_VALUE_PREFIX.length());
        return getEncryptor().decrypt(valueWithoutPrefix);
    }

    @Override
    public String prepareForStore(String value) {
        if (value.startsWith(ENCRYPTED_VALUE_PREFIX)) return value;
        String encryptedValue = getEncryptor().encrypt(value);
        return ENCRYPTED_VALUE_PREFIX + encryptedValue;
    }
}
