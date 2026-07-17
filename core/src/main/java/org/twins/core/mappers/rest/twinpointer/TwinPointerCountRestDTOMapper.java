package org.twins.core.mappers.rest.twinpointer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinpointer.TwinPointerCountDTOv1;
import org.twins.core.enums.sort.TwinPointerGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinPointerService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TwinPointerCountRestDTOMapper
        extends RestSimpleDTOMapper<CountResult<TwinPointerEntity, TwinPointerGroupField>, TwinPointerCountDTOv1> {

    @MapperModePointerBinding(modes = TwinClassMode.TwinPointer2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinPointer2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinPointer2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final TwinPointerService twinPointerService;

    @Override
    public void map(CountResult<TwinPointerEntity, TwinPointerGroupField> src, TwinPointerCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinClassId(entity.getTwinClassId())
                .setPointerFeaturerId(entity.getPointerFeaturerId())
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setOptional(entity.getOptional())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinClassMode.TwinPointer2TwinClassMode.HIDE, src, TwinPointerGroupField.twinClassId)) {
            twinPointerService.loadTwinClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinPointer2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, FeaturerMode.TwinPointer2FeaturerMode.HIDE, src, TwinPointerGroupField.pointerFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getPointerFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinPointer2FeaturerMode.SHORT)));
        }
        if (needLoad(mapperContext, UserMode.TwinPointer2UserMode.HIDE, src, TwinPointerGroupField.createdByUserId)) {
            twinPointerService.loadCreatedByUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinPointer2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinPointerEntity, TwinPointerGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinClassMode.TwinPointer2TwinClassMode.HIDE, someCount, TwinPointerGroupField.twinClassId)) {
            twinPointerService.loadTwinClass(entityCollection);
        }
        if (needLoad(mapperContext, UserMode.TwinPointer2UserMode.HIDE, someCount, TwinPointerGroupField.createdByUserId)) {
            twinPointerService.loadCreatedByUser(entityCollection);
        }
    }
}
