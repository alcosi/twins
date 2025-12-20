package org.twins.core.service;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.util.WebUtils;
import org.twins.core.service.auth.ActAsUserService;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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

    public String getActAsUserFromRequest() {
        return request.getHeader(HEADER_ACT_AS_USER);
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

}
