package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryCountDTOv1;
import org.twins.core.enums.sort.FactoryGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.factory.FactoryService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = UserMode.class)
public class FactoryCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryEntity, FactoryGroupField>, FactoryCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.Factory2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final FactoryService factoryService;

    @Override
    public void map(CountResult<TwinFactoryEntity, FactoryGroupField> src, FactoryCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setDomainId(entity.getDomainId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, UserMode.Factory2UserMode.HIDE, src, FactoryGroupField.createdByUserId)) {
            factoryService.loadCreatedByUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Factory2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryEntity, FactoryGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, UserMode.Factory2UserMode.HIDE, someCount, FactoryGroupField.createdByUserId)) {
            factoryService.loadCreatedByUser(entityCollection);
        }
    }
}
