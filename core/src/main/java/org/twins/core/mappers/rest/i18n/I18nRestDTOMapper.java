package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dto.rest.i18n.I18nDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.I18nMode;
import org.twins.core.service.i18n.I18nService;

import java.util.Collection;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = I18nMode.class)
public class I18nRestDTOMapper extends RestSimpleDTOMapper<I18nEntity, I18nDTOv1> {
    private final I18nService i18nService;

    @Override
    public void map(I18nEntity src, I18nDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(I18nMode.SHORT)) {
            case SHORT -> dst
                    .setI18nId(src.getId());
            case DETAILED -> dst
                    .setI18nId(src.getId())
                    .setKey(src.getKey())
                    .setName(src.getName());
        }
        if (showTranslations(mapperContext)) {
            dst.setTranslations(new HashMap<>());
            i18nService.loadTranslations(src);
            for (var entry : src.getTranslations()) {
                dst.getTranslations().put(entry.getLocale(), entry.getTranslation());
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<I18nEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showTranslations(mapperContext)) {
            i18nService.loadTranslations(srcCollection);
        }
        if (showTranslationStyles(mapperContext)) {
            //todo load styles
        }
    }

    public static boolean showTranslations(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(I18nMode.SHORT);
    }

    public static boolean showTranslationStyles(MapperContext mapperContext) {
        return mapperContext.hasMode(I18nMode.DETAILED);
    }
}
