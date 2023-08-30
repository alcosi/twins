package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class DataListOptionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv1> {

    @Override
    public void map(DataListOptionEntity entity, DataListOptionDTOv1 dto) {
        dto
                .id(entity.getId())
                .name(entity.getOption())
                .disabled(entity.isDisabled());
        ;
    }
}
