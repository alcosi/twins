package org.twins.core.mappers.rest.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dto.rest.i18n.I18nDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;

import java.util.Collection;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class I18nRestDTOMapper extends RestSimpleDTOMapper<I18nEntity, I18nDTOv1> {
    private final I18nService i18nService;

    @Override
    public void map(I18nEntity src, I18nDTOv1 dst, MapperContext mapperContext) throws Exception {
        i18nService.loadTranslations(src);
        dst
                .setI18nId(src.getId())
                .setTranslations(new HashMap<>());
        for (var entry : src.getTranslations() ) {
            dst.getTranslations().put(entry.getLocale(), entry.getTranslation());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<I18nEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        i18nService.loadTranslations(srcCollection);
    }
}
