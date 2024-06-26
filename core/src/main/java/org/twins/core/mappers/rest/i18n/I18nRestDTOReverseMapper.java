package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dto.I18nDTOv1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.auth.AuthService;


@Component
@RequiredArgsConstructor
public class I18nRestDTOReverseMapper extends RestSimpleDTOMapper<I18nDTOv1, I18nEntity> {
    final AuthService authService;
    @Autowired
    I18nTranslationRestDTOReverseMapper i18nTranslationRestDTOReverseMapper;

    @Override
    public void map(I18nDTOv1 src, I18nEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setType(src.getI18nType());
        if (src.getTranslationInCurrentLocale() != null)
            src.getTranslations().put(authService.getApiUser().getLocale(), src.getTranslationInCurrentLocale());
        for (var entry : src.getTranslations().entrySet()) {
            if (entry.getKey() == null)
                continue;
            dst.addTranslation(i18nTranslationRestDTOReverseMapper.convert(Pair.of(entry.getKey(), entry.getValue())));
        }
    }
}
