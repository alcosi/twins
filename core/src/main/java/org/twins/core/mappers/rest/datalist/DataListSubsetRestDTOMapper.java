package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dto.rest.datalist.DataListSubsetDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListSubsetMode;
import org.twins.core.service.datalist.DataListSubsetService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListSubsetMode.class)
public class DataListSubsetRestDTOMapper extends RestSimpleDTOMapper<DataListSubsetEntity, DataListSubsetDTOv1> {
    @MapperModePointerBinding(modes = DataListMode.DataListSubset2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;

    private final DataListSubsetService dataListSubsetService;

    @Override
    public void map(DataListSubsetEntity src, DataListSubsetDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (src == null) {
            return;
        }
        switch (mapperContext.getModeOrUse(DataListSubsetMode.DETAILED)) {
            case DETAILED ->
                    dst
                            .setId(src.getId())
                            .setDataListId(src.getDataListId())
                            .setName(src.getName())
                            .setDescription(src.getDescription())
                            .setKey(src.getKey());
            case SHORT ->
                    dst
                            .setId(src.getId())
                            .setDataListId(src.getDataListId())
                            .setKey(src.getKey());
        }
        if (mapperContext.hasModeButNot(DataListMode.DataListOption2DataListMode.HIDE)) {
            dst.setDataListId(src.getDataListId());
            dataListSubsetService.loadDataLists(src);
            dataListRestDTOMapper.postpone(src.getDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListOption2DataListMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DataListSubsetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(DataListMode.DataListOption2DataListMode.HIDE)) {
            dataListSubsetService.loadDataLists(srcCollection);
        }
    }
}
