package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.i18n.dao.I18nTranslationEntity;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.auth.AuthService;

import java.util.Locale;


@Component
@RequiredArgsConstructor
public class I18nTranslationRestDTOReverseMapper extends RestSimpleDTOMapper<Pair<Locale, String>, I18nTranslationEntity> {
    final AuthService authService;

    @Override
    public void map(Pair<Locale, String> src, I18nTranslationEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setLocale(src.getLeft())
                .setTranslation(src.getRight());
    }
}
