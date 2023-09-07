package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv2;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class DataListOptionRestDTOMapperV2 extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv2> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(DataListOptionEntity entity, DataListOptionDTOv2 dto, MapperProperties mapperProperties) {
        dataListOptionRestDTOMapper.map(entity, dto, mapperProperties);
        dto.dataListId(entity.dataListId());
    }
}
