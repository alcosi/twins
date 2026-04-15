package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinstatus.TwinStatusDuplicate;
import org.twins.core.dto.rest.twinstatus.TwinStatusDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinStatusDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusDuplicateDTOv1, TwinStatusDuplicate> {

    @Override
    public void map(TwinStatusDuplicateDTOv1 src, TwinStatusDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalTwinStatusId(src.getOriginalTwinStatusId())
                .setNewKey(src.getNewKey())
                .setDuplicateTriggers(src.isDuplicateTriggers())
        ;
    }
}
