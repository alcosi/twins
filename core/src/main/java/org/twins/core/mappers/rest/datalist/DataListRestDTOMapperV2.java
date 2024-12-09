package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.service.datalist.DataListService;


@Component
@RequiredArgsConstructor
public class DataListRestDTOMapperV2 extends RestSimpleDTOMapper<DataListEntity, DataListDTOv2> {

    @MapperModePointerBinding(modes = DataListOptionMode.class)
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    private final DataListService dataListService;

    private final DataListRestDTOMapper dataListRestDTOMapper;

    @Override
    public void map(DataListEntity src, DataListDTOv2 dst, MapperContext mapperContext) throws Exception {
        dataListRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(DataListOptionMode.HIDE)) {
            dataListService.loadDataListOptions(src);
//            dst.setOptions(dataListOptionRestDTOMapper.convertMap(src.getOptions().getMap(), mapperContext)); //todo remove me after gateway support of relateMap of dataListOptions
            convertMapOrPostpone(src.getOptions(), dst, dataListOptionRestDTOMapper, mapperContext, DataListDTOv2::setOptions, DataListDTOv2::setOptionIdList);
        }
    }

}
