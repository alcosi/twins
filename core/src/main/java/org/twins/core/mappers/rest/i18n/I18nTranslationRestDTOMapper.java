package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dto.rest.i18n.I18nTranslationDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.I18nMode;
import org.twins.core.mappers.rest.mappercontext.modes.I18nTranslationMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = I18nTranslationMode.class)
public class I18nTranslationRestDTOMapper extends RestSimpleDTOMapper<I18nTranslationEntity, I18nTranslationDTOv1> {
    @MapperModePointerBinding(modes = I18nMode.I18nTranslation2I18nMode.class)
    private final I18nRestDTOMapper i18nRestDTOMapper;

    @Override
    public void map(I18nTranslationEntity src, I18nTranslationDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(I18nTranslationMode.DETAILED)) {
            case SHORT, DETAILED ->
                    dst
                            .setI18nId(src.getI18nId())
                            .setLocale(src.getLocale())
                            .setTranslation(src.getTranslation());
        }
        if (mapperContext.hasModeButNot(I18nMode.I18nTranslation2I18nMode.HIDE)) {
            i18nRestDTOMapper.postpone(src.getI18n(), mapperContext.forkOnPoint(I18nMode.I18nTranslation2I18nMode.SHORT));
        }
    }
}
