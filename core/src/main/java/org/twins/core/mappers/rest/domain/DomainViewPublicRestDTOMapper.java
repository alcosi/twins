package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainViewPublicDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainMode;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.resource.ResourceService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class DomainViewPublicRestDTOMapper extends RestSimpleDTOMapper<DomainEntity, DomainViewPublicDTOv1> {
    protected final ResourceService resourceService;
    protected final DomainService domainService;

    @Override
    public void map(DomainEntity src, DomainViewPublicDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (mapperContext.getModeOrUse(DomainMode.DETAILED) == DomainMode.DETAILED)
            domainService.loadIconResources(src);

        switch (mapperContext.getModeOrUse(DomainMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setDescription(src.getDescription())
                        .setIconDark(resourceService.getResourceUri(src.getIconDarkResource()))
                        .setIconLight(resourceService.getResourceUri(src.getIconLightResource()));
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.getModeOrUse(DomainMode.DETAILED) == DomainMode.DETAILED)
            domainService.loadIconResources(srcCollection);
    }
}
