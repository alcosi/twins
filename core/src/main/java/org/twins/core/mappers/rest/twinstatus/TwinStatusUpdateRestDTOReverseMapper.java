package org.twins.core.mappers.rest.twinstatus;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class TwinStatusUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusUpdateDTOv1, TwinStatusEntity> {
    @Override
    public void map(TwinStatusUpdateDTOv1 src, TwinStatusEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setBackgroundColor(src.getBackgroundColor())
                .setFontColor(src.getFontColor())
                .setType(src.getType())
        ;
    }
}
