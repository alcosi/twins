package org.twins.core.mappers.rest.twinstatus;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class TwinStatusCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusCreateDTOv1, TwinStatusEntity> {
    @Override
    public void map(TwinStatusCreateDTOv1 src, TwinStatusEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setInheritable(src.getInheritable())
                .setKey(src.getKey())
                .setBackgroundColor(src.getBackgroundColor())
                .setFontColor(src.getFontColor())
                .setType(src.getType())
        ;
    }
}
