package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinFieldI18nEntity;
import org.twins.core.dto.rest.twin.TwinFieldI18nDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.I18nTranslationMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinI18nFieldMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinI18nFieldMode.class)
public class TwinFieldI18nRestDTOMapper extends RestSimpleDTOMapper<TwinFieldI18nEntity, TwinFieldI18nDTOv1> {

    @Override
    public void map(TwinFieldI18nEntity src, TwinFieldI18nDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(I18nTranslationMode.DETAILED)) {
            case SHORT, DETAILED ->
                    dst
                            .setLocale(src.getLocale())
                            .setTranslation(src.getTranslation());
        }
    }
}
