package org.twins.core.mappers.rest.i18n;

import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dto.rest.i18n.I18nTranslationUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class I18nTranslationUpdateDTOReverseMapper extends RestSimpleDTOMapper<I18nTranslationUpdateDTOv1, I18nTranslationEntity> {
    @Override
    public void map(I18nTranslationUpdateDTOv1 src, I18nTranslationEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setI18nId(src.getI18nId())
                .setLocale(src.getLocale())
                .setTranslation(src.getTranslation());
    }
}
