package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.domain.DomainViewDTOv1;
import org.twins.core.featurer.resource.StoragerFileService;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainMode;
import org.twins.core.service.domain.DomainService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class DomainViewRestDTOMapper extends RestSimpleDTOMapper<DomainEntity, DomainViewDTOv1> {
    protected final FeaturerService featurerService;
    protected final DomainService domainService;

    @Override
    public List<DomainViewDTOv1> convertCollection(Collection<DomainEntity> srcCollection, MapperContext mapperContext) throws Exception {
        loadResourcesList(srcCollection, mapperContext);
        return super.convertCollection(srcCollection, mapperContext);
    }


    @Override
    public Map<UUID, DomainViewDTOv1> convertMap(Map<UUID, DomainEntity> srcMap, MapperContext mapperContext) throws Exception {
        loadResourcesList(srcMap.values(), mapperContext);
        return super.convertMap(srcMap, mapperContext);
    }

    protected void loadResourcesList(Collection<DomainEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.getModeOrUse(DomainMode.DETAILED) == DomainMode.DETAILED) {
            domainService.loadResources(srcCollection);
        }
    }
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
            var featurer = featurerService.getFeaturer(icon.getStorage().getStorageFeaturer(), StoragerFileService.class);
            return featurer.getFileUri(icon.getId(), icon.getStorageFileKey(), icon.getStorage().getStorageParams()).toString();
        }
        return null;
    }

}
