package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.dto.rest.twin.TwinBasicFieldsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinBasicFieldsRestDTOReverseMapper extends RestSimpleDTOMapper<TwinBasicFieldsDTOv1, TwinBasicFields> {

    @Override
    public void map(TwinBasicFieldsDTOv1 src, TwinBasicFields dst, MapperContext mapperContext) throws Exception {
        dst
                .setCreatedByUserId(src.getCreatedByUserId())
                .setAssigneeUserId(src.getAssigneeUserId())
                .setName(src.getName())
                .setDescription(src.getDescription());
    }
}
