package org.twins.core.mappers.rest.locale;

import org.cambium.i18n.dao.I18nLocaleEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.domain.LocaleDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class LocaleRestDTOMapper extends RestSimpleDTOMapper<I18nLocaleEntity, LocaleDTOv1> {
    @Override
    public void map(I18nLocaleEntity src, LocaleDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getLocale())
                .setName(src.getName())
                .setNativeName(src.getNativeName())
                .setIcon(src.getIcon());
    }
}
