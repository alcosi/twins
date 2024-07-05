package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainAddRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class DomainAddRestDTOReverseMapper extends RestSimpleDTOMapper<DomainAddRqDTOv1, DomainEntity> {

    private final I18nService i18nService;

    @Override
    public void map(DomainAddRqDTOv1 src, DomainEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.key())
                .setDescription(src.description())
                .setDomainType(src.type)
                .setDefaultI18nLocaleId(i18nService.localeFromTagOrSystemDefault(src.defaultLocale));
    }
}
