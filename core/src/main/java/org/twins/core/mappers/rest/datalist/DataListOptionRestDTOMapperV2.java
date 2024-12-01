package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;


@Component
@RequiredArgsConstructor
public class DataListOptionRestDTOMapperV2 extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv2> {

    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(DataListOptionEntity src, DataListOptionDTOv2 dst, MapperContext mapperContext) {
        dataListOptionRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(DataListOptionMode.HIDE)) {
            dst
                    .setDataListId(src.getDataListId())
                    .setBusinessAccountId(src.getBusinessAccountId());
        }
    }
}
