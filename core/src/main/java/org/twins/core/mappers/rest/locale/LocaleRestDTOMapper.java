package org.twins.core.mappers.rest.locale;

import org.cambium.i18n.dao.I18nLocaleEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.domain.DomainLocaleDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class LocaleRestDTOMapper extends RestSimpleDTOMapper<I18nLocaleEntity, DomainLocaleDTOv1> {
    @Override
    public void map(I18nLocaleEntity src, DomainLocaleDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getLocale().toLanguageTag())
                .setName(src.getName())
                .setNativeName(src.getNativeName())
                .setIcon(src.getIcon());
    }
}
