package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.datalist.DataListCountDTOv1;
import org.twins.core.enums.sort.DataListGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.datalist.DataListService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListMode.class)
public class DataListCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DataListEntity, DataListGroupField>, DataListCountDTOv1> {

    @MapperModePointerBinding(modes = UserMode.DataList2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final DataListService dataListService;

    @Override
    public void map(CountResult<DataListEntity, DataListGroupField> src, DataListCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, UserMode.DataList2UserMode.HIDE, src, DataListGroupField.createdByUserId)) {
            dataListService.loadCreatedByUser(entity);
            userRestDTOMapper.convertOrPostpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DataList2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DataListEntity, DataListGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, UserMode.DataList2UserMode.HIDE, sample, DataListGroupField.createdByUserId)) {
            dataListService.loadCreatedByUsers(entityCollection);
        }
    }
}
