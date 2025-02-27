package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dto.rest.i18n.I18nTranslationDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.I18nTranslationMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = I18nTranslationMode.class)
public class I18nTranslationRestDTOMapper extends RestSimpleDTOMapper<I18nTranslationEntity, I18nTranslationDTOv1> {
    @Override
    public void map(I18nTranslationEntity src, I18nTranslationDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassFieldMode.DETAILED)) {
            case SHORT -> dst
                    .setI18nId(src.getI18nId())
                    .setLocale(src.getLocale());
            case DETAILED -> dst
                    .setI18nId(src.getI18nId())
                    .setLocale(src.getLocale())
                    .setTranslation(src.getTranslation());
        }
    }
}
