package org.twins.core.featurer.identityprovider.trustor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.ActAsUser;
import org.twins.core.domain.auth.CryptKey;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Featurer(id = FeaturerTwins.ID_3502,
        name = "Encrypted",
        description = "RSA key encryption")
@RequiredArgsConstructor
@Slf4j
public class TrustorEncrypted extends Trustor {
    private final AuthService authService;

    @Override
    public CryptKey.CryptPublicKey getActAsUserPublicKey(Properties properties) throws ServiceException {
        return getKey().getPublicKey();
    }

    @Override
    public ActAsUser resolveActAsUser(Properties properties, String actAsUserHeader) throws ServiceException {
        byte[] decodedBytes = Base64.getDecoder().decode(actAsUserHeader);
        String jsonString = new String(decodedBytes);
        try {
            Map<String, String> payload = objectMapper.readValue(jsonString, Map.class);
            String encryptedKeyBase64 = payload.get(ACT_AS_USER_HEADER_ENCRYPTED_KEY);
            String ivBase64 = payload.get(ACT_AS_USER_HEADER_IV);
            String ciphertextBase64 = payload.get(ACT_AS_USER_HEADER_CIPHER_TEXT);

            byte[] encryptedKey = Base64.getDecoder().decode(encryptedKeyBase64);
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] ciphertext = Base64.getDecoder().decode(ciphertextBase64);

            return decrypt(encryptedKey, iv, ciphertext);
        } catch (Exception e) {
            log.error("Act as user exception:", e);
            throw new RuntimeException(e);
        }
    }

    private final ConcurrentMap<UUID, CryptKey> domainKeysMap = new ConcurrentHashMap<>();

    public CryptKey getKey() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        CryptKey domainKey = domainKeysMap.computeIfAbsent(apiUser.getDomainId(), k -> new CryptKey().setExpires(LocalDateTime.now()));
        if (domainKey.getExpires().isBefore(LocalDateTime.now())) {
            //refresh
            try {
                domainKey.flush();
            } catch (NoSuchAlgorithmException e) {
                throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION);
            }
        }
        return domainKey;
    }

    public ObjectMapper objectMapper = new ObjectMapper();

    public static final String ACT_AS_USER_USER_ID = "userId";
    public static final String ACT_AS_USER_BUSINESS_ACCOUNT_ID = "businessAccountId";
    public static final String ACT_AS_USER_NONCE_ID = "nonceId";

    public static final String ACT_AS_USER_HEADER_ENCRYPTED_KEY = "encrypted_key";
    public static final String ACT_AS_USER_HEADER_IV = "iv";
    public static final String ACT_AS_USER_HEADER_CIPHER_TEXT = "ciphertext";


    public ActAsUser decrypt(byte[] encryptedKey, byte[] iv, byte[] ciphertext) throws ServiceException {
        CryptKey cryptKey = getKey();
        ActAsUser actAsUser = null;
        UUID nonce = null;
        try {
            PrivateKey privateKey = cryptKey.getKeyPair().getPrivate();

            //AES-key decryption (RSA)
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKey = rsaCipher.doFinal(encryptedKey);

            //ciphertext (AES-GCM) decryption
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag length
            aesCipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decryptedBytes = aesCipher.doFinal(ciphertext);
            String decryptedJson = new String(decryptedBytes, java.nio.charset.StandardCharsets.UTF_8);
            Map<String, String> payload = objectMapper.readValue(decryptedJson, Map.class);
            actAsUser = new ActAsUser()
                    .setUserId(UUID.fromString(payload.get(ACT_AS_USER_USER_ID)))
                    .setBusinessAccountId(UuidUtils.fromStringOrNull(payload.get(ACT_AS_USER_BUSINESS_ACCOUNT_ID)));
            nonce = UuidUtils.fromStringOrNull(payload.get(ACT_AS_USER_NONCE_ID));
        } catch (Exception e) {
            log.error("Act as user exception:", e);
            throw new ServiceException(ErrorCodeTwins.ACT_AS_USER_INCORRECT);
        }
        if (nonce != null && cryptKey.isAlreadyProcessed(nonce)) {
            throw new ServiceException(ErrorCodeTwins.ACT_AS_USER_NONCE_IS_NOT_UNIQ);
        }
        return actAsUser;
    }
}
