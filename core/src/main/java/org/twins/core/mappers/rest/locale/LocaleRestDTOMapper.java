package org.twins.core.mappers.rest.locale;

import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainLocaleEntity;
import org.twins.core.dto.rest.domain.LocaleDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class LocaleRestDTOMapper extends RestSimpleDTOMapper<DomainLocaleEntity, LocaleDTOv1> {
    @Override
    public void map(DomainLocaleEntity src, LocaleDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getLocale())
                .setName(src.getI18nLocale().getName())
                .setNativeName(src.getI18nLocale().getNativeName())
                .setIcon(src.getIcon());
    }
}
