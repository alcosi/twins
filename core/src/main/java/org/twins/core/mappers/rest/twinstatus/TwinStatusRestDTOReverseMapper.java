package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinStatusRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusUpdateRqDTOv1, TwinStatusEntity> {
    @Override
    public void map(TwinStatusUpdateRqDTOv1 src, TwinStatusEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getTwinStatus().getId())
                .setKey(src.getTwinStatus().getKey())
                .setColor(src.getTwinStatus().getColor())
                .setLogo(src.getTwinStatus().getLogo())
        ;
    }
}
