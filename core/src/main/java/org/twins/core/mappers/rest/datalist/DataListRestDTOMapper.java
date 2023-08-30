package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.mappers.rest.RestListDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class DataListRestDTOMapper extends RestSimpleDTOMapper<DataListEntity, DataListDTOv1> {
    @Override
    public void map(DataListEntity entity, DataListDTOv1 dto) {
        dto
                .id(entity.getId())
                .name(entity.getName())
                .updatedAt(entity.getUpdatedAt().toInstant())
                .description(entity.getDescription());
        ;
    }
}
