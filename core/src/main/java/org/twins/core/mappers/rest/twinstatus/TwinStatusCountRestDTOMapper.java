package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinstatus.TwinStatusCountDTOv1;
import org.twins.core.enums.sort.TwinStatusGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.twin.TwinStatusService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TwinStatusCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinStatusEntity, TwinStatusGroupField>, TwinStatusCountDTOv1> {
    @MapperModePointerBinding(modes = TwinClassMode.TwinStatus2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final TwinStatusService twinStatusService;

    @Override
    public void map(CountResult<TwinStatusEntity, TwinStatusGroupField> src, TwinStatusCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinClassId(entity.getTwinClassId())
                .setInheritable(entity.getInheritable())
                .setType(entity.getType())
                .setCount(src.getCount());

        if (needLoad(mapperContext, TwinClassMode.TwinStatus2TwinClassMode.HIDE, src, TwinStatusGroupField.twinClassId)) {
            twinStatusService.loadClass(entity);
            twinClassRestDTOMapper.postpone(entity.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.TwinStatus2TwinClassMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinStatusEntity, TwinStatusGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinClassMode.TwinStatus2TwinClassMode.HIDE, someCount, TwinStatusGroupField.twinClassId)) {
            twinStatusService.loadClass(entities);
        }
    }
}
