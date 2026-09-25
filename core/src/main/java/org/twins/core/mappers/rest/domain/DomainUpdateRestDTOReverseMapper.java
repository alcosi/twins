package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;


@Component
@RequiredArgsConstructor
public class DomainUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<DomainUpdateDTOv1, DomainEntity> {
    private final I18nService i18nService;

    @Override
    public void map(DomainUpdateDTOv1 src, DomainEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setName(src.getName())
                .setDescription(src.getDescription())
                .setDefaultI18nLocaleId(i18nService.localeFromTagOrSystemDefault(src.getDefaultLocale()))
                .setBusinessAccountInitiatorFeaturerId(src.getBusinessAccountInitiatorFeaturerId())
                .setBusinessAccountInitiatorParams(src.getBusinessAccountInitiatorParams())
                .setUserGroupManagerFeaturerId(src.getUserGroupManagerFeaturerId())
                .setUserGroupManagerParams(src.getUserGroupManagerParams())
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setTwinClassSchemaId(src.getTwinClassSchemaId())
                .setBusinessAccountTemplateTwinId(src.getBusinessAccountTemplateTwinId())
                .setDefaultTierId(src.getDefaultTierId())
                .setDomainUserTemplateTwinId(src.getDomainUserTemplateTwinId())
                .setIconDarkResourceId(src.getIconDarkResourceId())
                .setIconLightResourceId(src.getIconLightResourceId())
                .setResourcesStorageId(src.getResourceStorageId())
                .setAttachmentsStorageId(src.getAttachmentStorageId())
                .setNavbarFaceId(src.getNavbarFaceId());
    }
}
