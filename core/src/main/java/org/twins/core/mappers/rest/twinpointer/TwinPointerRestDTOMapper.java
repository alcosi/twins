package org.twins.core.mappers.rest.twinpointer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dto.rest.twinpointer.TwinPointerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinPointerMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinPointerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinPointerMode.class)
public class TwinPointerRestDTOMapper extends RestSimpleDTOMapper<TwinPointerEntity, TwinPointerDTOv1> {

    @MapperModePointerBinding(modes = TwinClassMode.TwinPointer2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.TwinPointer2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinPointer2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final TwinPointerService twinPointerService;

    @Override
    public void map(TwinPointerEntity src, TwinPointerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinPointerMode.DETAILED)) {
            case DETAILED ->
                    dst
                            .setId(src.getId())
                            .setName(src.getName())
                            .setTwinClassId(src.getTwinClassId())
                            .setPointerFeaturerId(src.getPointerFeaturerId())
                            .setPointerParams(src.getPointerParams())
                            .setOptional(src.getOptional())
                            .setCreatedAt(src.getCreatedAt());
            case SHORT ->
                    dst
                            .setId(src.getId())
                            .setName(src.getName());
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinPointer2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinPointerService.loadTwinClass(src);
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinPointer2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FeaturerMode.TwinPointer2FeaturerMode.HIDE)) {
            dst.setPointerFeaturerId(src.getPointerFeaturerId());
            featurerRestDTOMapper.postpone(src.getPointerFeaturerId(), mapperContext.forkOnPoint(FeaturerMode.TwinPointer2FeaturerMode.SHORT));
        }
        if (mapperContext.hasModeButNot(UserMode.TwinPointer2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            twinPointerService.loadCreatedByUser(src);
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.TwinPointer2UserMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinPointerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (srcCollection.isEmpty()) return;
        // Batch-load twinClass / createdByUser (N+1 prevention); featurer is resolved per-id via FeaturerRestDTOMapper.postpone(Integer)
        if (mapperContext.hasModeButNot(TwinClassMode.TwinPointer2TwinClassMode.HIDE)) {
            twinPointerService.loadTwinClass(srcCollection);
        }
        if (mapperContext.hasModeButNot(UserMode.TwinPointer2UserMode.HIDE)) {
            twinPointerService.loadCreatedByUser(srcCollection);
        }
    }
}
