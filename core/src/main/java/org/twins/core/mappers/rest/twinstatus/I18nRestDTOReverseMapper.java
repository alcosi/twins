package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.domain.I18nTranslation;
import org.cambium.i18n.dto.I18nDTOv1;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.auth.AuthService;


@Component
@RequiredArgsConstructor
public class I18nRestDTOReverseMapper extends RestSimpleDTOMapper<I18nDTOv1, I18nTranslation> {
    final AuthService authService;

    @Override
    public void map(I18nDTOv1 src, I18nTranslation dst, MapperContext mapperContext) throws Exception {
        if (src.getTranslationInCurrentLocale() != null)
            src.getTranslations().put(authService.getApiUser().getLocale(), src.getTranslationInCurrentLocale());
        dst
                .setTranslations(src.getTranslations());
    }
}
