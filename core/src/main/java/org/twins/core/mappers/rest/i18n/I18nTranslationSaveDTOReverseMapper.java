package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dto.rest.i18n.I18nTranslationSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class I18nTranslationSaveDTOReverseMapper extends RestSimpleDTOMapper<I18nTranslationSaveDTOv1, I18nTranslationEntity> {
    @Override
    public void map(I18nTranslationSaveDTOv1 src, I18nTranslationEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setLocale(src.getLocale())
                .setTranslation(src.getTranslation());
    }
}
