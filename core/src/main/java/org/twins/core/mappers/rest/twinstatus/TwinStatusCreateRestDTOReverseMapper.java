package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinStatusCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusCreateRqDTOv1, TwinStatusEntity> {
    @Override
    public void map(TwinStatusCreateRqDTOv1 src, TwinStatusEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setKey(src.getKey())
                .setLogo(src.getLogo())
                .setColor(src.getColor())
        ;
    }
}
