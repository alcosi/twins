package org.twins.core.config.advice;

import org.cambium.common.util.MapUtils;
import org.cambium.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.service.i18n.I18nService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ControllerAdvice
public class I18nResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final I18nService i18nService;

    public I18nResponseBodyAdvice(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    public boolean supports(@NotNull MethodParameter returnType,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NotNull MethodParameter returnType,
                                  @NotNull MediaType selectedContentType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NotNull org.springframework.http.server.ServerHttpRequest request,
                                  @NotNull org.springframework.http.server.ServerHttpResponse response) {

        Set<UUID> idsToLoad = I18nCacheHolder.getIdsToLoad();
        Map<UUID, Map<String, String>> contexts = I18nCacheHolder.getContexts();
        if (!idsToLoad.isEmpty() && I18nCacheHolder.getTranslations().isEmpty()) {
            Map<UUID, String> translations = i18nService.translateToLocale(idsToLoad);

            for (Map.Entry<UUID, Map<String, String>> entry : contexts.entrySet()) {
                UUID i18nId = entry.getKey();
                Map<String, String> context = entry.getValue();
                String translation = translations.get(i18nId);

                if (StringUtils.isNotBlank(translation) && MapUtils.isNotEmpty(context)) {
                    translations.put(i18nId, StringUtils.replaceVariables(translation, context));
                }
            }

            I18nCacheHolder.setTranslations(translations);
        }

        return body;
    }
}