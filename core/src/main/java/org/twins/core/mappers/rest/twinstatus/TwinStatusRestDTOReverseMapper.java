package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TwinStatusRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusUpdateRqDTOv1, TwinStatusEntity> {
    @Override
    public void map(TwinStatusUpdateRqDTOv1 src, TwinStatusEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setBackgroundColor(src.getBackgroundColor())
                .setFontColor(src.getFontColor())
                .setType(src.getType())
        ;
    }
}
