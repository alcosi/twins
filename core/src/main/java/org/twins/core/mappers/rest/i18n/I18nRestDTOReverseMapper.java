package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.twins.core.i18n.dao.I18nEntity;
import org.twins.core.i18n.dto.I18nDTOv1;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.auth.AuthService;

import java.util.HashMap;


@Component
@RequiredArgsConstructor
public class I18nRestDTOReverseMapper extends RestSimpleDTOMapper<I18nDTOv1, I18nEntity> {

    private final I18nTranslationRestDTOReverseMapper i18nTranslationRestDTOReverseMapper;
    private final AuthService authService;

    @Override
    public void map(I18nDTOv1 src, I18nEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setType(src.getI18nType());
        if (src.getTranslations() == null)
            src.setTranslations(new HashMap<>());
        if (src.getTranslationInCurrentLocale() != null)
            src.getTranslations().put(authService.getApiUser().getLocale(), src.getTranslationInCurrentLocale());
        for (var entry : src.getTranslations().entrySet()) {
            if (entry.getKey() == null)
                continue;
            dst.addTranslation(i18nTranslationRestDTOReverseMapper.convert(Pair.of(entry.getKey(), entry.getValue())));
        }
    }
}
