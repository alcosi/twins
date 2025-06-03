package org.twins.core.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.util.WebUtils;
import org.twins.core.domain.apiuser.ActAsUser;
import org.twins.core.service.auth.ActAsUserService;

import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class HttpRequestService extends SessionLocaleResolver {
    public static final String HEADER_LOCALE = "Locale";
    public static final String HEADER_USER_ID = "UserId";
    public static final String HEADER_AUTH_TOKEN = "AuthToken";
    public static final String HEADER_DOMAIN_ID = "DomainId";
    public static final String HEADER_BUSINESS_ACCOUNT_ID = "BusinessAccountId";
    public static final String HEADER_CHANNEL = "Channel";
    public static final String HEADER_ACT_AS_USER = "X-Act-As-User";

    private final ActAsUserService actAsUserService;

    public HttpServletRequest getRequest() {
        return request;
    }

    private final HttpServletRequest request;

    public String getUserIdFromRequest() {
        return request.getHeader(HEADER_USER_ID);
    }
    public String getAuthTokenFromRequest() {
        return request.getHeader(HEADER_AUTH_TOKEN);
    }
    public String getDomainIdFromRequest() {
        return request.getHeader(HEADER_DOMAIN_ID);
    }

    public String getBusinessAccountIdFromRequest() {
        return request.getHeader(HEADER_BUSINESS_ACCOUNT_ID);
    }

    public String getChannelIdFromRequest() {
        return request.getHeader(HEADER_CHANNEL);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return getHeaderLocale().orElse(Locale.ENGLISH);
//        Locale locale = Locale.ENGLISH;
//                getSessionLocale(request)
//                        .orElseGet(() -> getHeaderLocale()
//                                .orElseGet(() -> {
//                                    String token = request.getHeader("Authorization");
//                                    if (token == null || token.isEmpty()) {
//                                        return BaseApplicationConfig.APP_DEFAULT_LOCALE;
//                                    } else {
//                                        try {
//                                            return Optional
//                                                    .ofNullable(getClientFromRequest())
//                                                    .map(ClientEntityBase::getLocale)
//                                                    .orElse(BaseApplicationConfig.APP_DEFAULT_LOCALE);
//                                        } catch (Throwable e) {
//                                            return BaseApplicationConfig.APP_DEFAULT_LOCALE;
//                                        }
//                                    }
//                                }));
//        log.trace("Locale resolved {}", locale);
//        return locale;
    }

    private Optional<Locale> getSessionLocale(HttpServletRequest request) {
        try {
            Locale sessionAttribute = (Locale) WebUtils.getSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME);
            log.trace("Session locale :{}", sessionAttribute);
            return Optional.ofNullable(sessionAttribute);
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public Optional<Locale> getHeaderLocale() {
        try {
            String language = request.getHeader(HEADER_LOCALE);
            Locale locale = new Locale("ua".equalsIgnoreCase(language) ? "uk" : language);
            log.trace("Header locale :{}", locale);
            return Optional.of(locale);
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        super.setLocale(request, response, locale);
    }
    

    public Locale getLocale() {
        return resolveLocale(request);
    }

    public static final String ACT_AS_USER_ENCRYPTED_KEY = "encrypted_key";
    public static final String ACT_AS_USER_IV = "iv";
    public static final String ACT_AS_USER_CIPHER_TEXT = "ciphertext";

    private ObjectMapper objectMapper = new ObjectMapper();

    public ActAsUser getActAsUser() {
        String actAsUserHeader = request.getHeader(HEADER_ACT_AS_USER);
        if (StringUtils.isEmpty(actAsUserHeader))
            return null;
        // Base64 decode
        byte[] decodedBytes = Base64.getDecoder().decode(actAsUserHeader);
        String jsonString = new String(decodedBytes);

        try {
            Map<String, String> payload = objectMapper.readValue(jsonString, Map.class);

            String encryptedKeyBase64 = payload.get(ACT_AS_USER_ENCRYPTED_KEY);
            String ivBase64 = payload.get(ACT_AS_USER_IV);
            String ciphertextBase64 = payload.get(ACT_AS_USER_CIPHER_TEXT);

            byte[] encryptedKey = Base64.getDecoder().decode(encryptedKeyBase64);
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] ciphertext = Base64.getDecoder().decode(ciphertextBase64);

            return actAsUserService.decrypt(encryptedKey, iv, ciphertext);
        } catch (Exception e) {
            log.error("Act as user exception:", e);
            throw new RuntimeException(e);
        }
    }
    
}
