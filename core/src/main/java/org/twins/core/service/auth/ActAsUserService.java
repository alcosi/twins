package org.twins.core.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.ActAsUser;
import org.twins.core.domain.auth.CryptKey;
import org.twins.core.exception.ErrorCodeTwins;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class ActAsUserService  {
    private final AuthService authService;
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

    public CryptKey.CryptPublicKey getPublicKey() throws ServiceException {
        return getKey().getPublicKey();
    }

    public ObjectMapper objectMapper = new ObjectMapper();

    public static final String ACT_AS_USER_USER_ID = "userId";
    public static final String ACT_AS_USER_BUSINESS_ACCOUNT_ID = "businessAccountId";
    public static final String ACT_AS_USER_NONCE_ID = "nonceId";

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
