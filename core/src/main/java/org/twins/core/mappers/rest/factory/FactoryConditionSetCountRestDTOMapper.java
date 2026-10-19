package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryConditionSetCountDTOv1;
import org.twins.core.enums.sort.FactoryConditionSetGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.factory.FactoryConditionSetService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMode.class, UserMode.class})
public class FactoryConditionSetCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryConditionSetEntity, FactoryConditionSetGroupField>, FactoryConditionSetCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryMode.FactoryConditionSet2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.FactoryConditionSet2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final FactoryConditionSetService factoryConditionSetService;

    @Override
    public void map(CountResult<TwinFactoryConditionSetEntity, FactoryConditionSetGroupField> src, FactoryConditionSetCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinFactoryId(entity.getTwinFactoryId())
                .setCachable(entity.getCachable())
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryMode.FactoryConditionSet2FactoryMode.HIDE, src, FactoryConditionSetGroupField.twinFactoryId)) {
            factoryConditionSetService.loadFactory(entity);
            factoryRestDTOMapper.convertOrPostpone(entity.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryConditionSet2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, UserMode.FactoryConditionSet2UserMode.HIDE, src, FactoryConditionSetGroupField.createdByUserId)) {
            factoryConditionSetService.loadCreatedByUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.FactoryConditionSet2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryConditionSetEntity, FactoryConditionSetGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryMode.FactoryConditionSet2FactoryMode.HIDE, someCount, FactoryConditionSetGroupField.twinFactoryId)) {
            factoryConditionSetService.loadFactory(entityCollection);
        }
        if (needLoad(mapperContext, UserMode.FactoryConditionSet2UserMode.HIDE, someCount, FactoryConditionSetGroupField.createdByUserId)) {
            factoryConditionSetService.loadCreatedByUser(entityCollection);
        }
    }
}
