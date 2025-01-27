package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.domain.DomainViewDTOv1;
import org.twins.core.featurer.resource.StorageFileService;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainMode;

import java.util.HashMap;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class DomainViewRestDTOMapper extends RestSimpleDTOMapper<DomainEntity, DomainViewDTOv1> {
    protected final FeaturerService featurerService;

    @Override
    public void map(DomainEntity src, DomainViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        String darkIconUri = getIconUri(src.getIconDarkResource());
        String lightIconUri = getIconUri(src.getIconLightResource());
        switch (mapperContext.getModeOrUse(DomainMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setDescription(src.getDescription())
                        .setType(src.getDomainType())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setDefaultLocale(src.getDefaultI18nLocaleId() != null ? src.getDefaultI18nLocaleId().getLanguage() : null)
                        .setTwinflowSchemaId(src.getTwinflowSchemaId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setTwinClassSchemaId(src.getTwinClassSchemaId())
                        .setBusinessAccountTemplateTwinId(src.getBusinessAccountTemplateTwinId())
                        .setIconDark(darkIconUri)
                        .setIconLight(lightIconUri);
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
    }

    private String getIconUri(ResourceEntity icon) throws ServiceException {
        if (icon != null) {
            var featurer = featurerService.getFeaturer(icon.getStorage().getStorageFeaturer(), StorageFileService.class);
            return featurer.getFileUri(icon.getId(), icon.getStorageFileKey(), icon.getStorage().getStorageParams(), new HashMap<>()).toString();
        }
        return null;
    }

}
