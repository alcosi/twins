package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;

@Component
@RequiredArgsConstructor
public class DomainSaveRestDTOReverseMapper extends RestSimpleDTOMapper<DomainSaveDTOv1, DomainEntity> {
    private final I18nService i18nService;

    @Override
    public void map(DomainSaveDTOv1 src, DomainEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setName(src.getName())
                .setDescription(src.getDescription())
                .setDefaultI18nLocaleId(i18nService.localeFromTagOrSystemDefault(src.getDefaultLocale()));
    }
}
