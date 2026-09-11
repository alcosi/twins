package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.datalist.DataListSubsetCountDTOv1;
import org.twins.core.enums.sort.DataListSubsetGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.service.datalist.DataListSubsetService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DataListSubsetCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<DataListSubsetEntity, DataListSubsetGroupField>, DataListSubsetCountDTOv1> {

    @MapperModePointerBinding(modes = DataListMode.DataListSubset2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;

    private final DataListSubsetService dataListSubsetService;

    @Override
    public void map(CountResult<DataListSubsetEntity, DataListSubsetGroupField> src, DataListSubsetCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setDataListId(entity.getDataListId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, DataListMode.DataListSubset2DataListMode.HIDE, src, DataListSubsetGroupField.dataListId)) {
            dataListSubsetService.loadDataLists(entity);
            dataListRestDTOMapper.convertOrPostpone(entity.getDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListSubset2DataListMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<DataListSubsetEntity, DataListSubsetGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, DataListMode.DataListSubset2DataListMode.HIDE, sample, DataListSubsetGroupField.dataListId)) {
            dataListSubsetService.loadDataLists(entityCollection);
        }
    }
}
