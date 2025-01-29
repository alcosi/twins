package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.twins.core.service.domain.DomainService.DEFAULT_RESOURCE_STORAGE_ID;


@Component
@RequiredArgsConstructor
public class DomainAddRestDTOReverseMapper extends RestSimpleDTOMapper<DomainCreateRqDTOv1, DomainEntity> {

    private final I18nService i18nService;

    @Override
    public void map(DomainCreateRqDTOv1 src, DomainEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.key())
                .setDescription(src.description())
                .setDomainType(src.type)
                .setResourcesStorageId(src.resourceStorageId == null ? DEFAULT_RESOURCE_STORAGE_ID : src.resourceStorageId)
                .setAttachmentsStorageId(src.attachmentStorageId == null ? DEFAULT_RESOURCE_STORAGE_ID : src.attachmentStorageId)
                .setDefaultI18nLocaleId(i18nService.localeFromTagOrSystemDefault(src.defaultLocale));
    }
}
