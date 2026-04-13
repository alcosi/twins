package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassDuplicate;
import org.twins.core.dto.rest.twinclass.TwinClassDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassDuplicateDTOv1, TwinClassDuplicate> {

    @Override
    public void map(TwinClassDuplicateDTOv1 src, TwinClassDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalTwinClassId(src.getOriginalTwinClassId())
                .setNewKey(src.getNewKey())
                .setDuplicateFields(src.isDuplicateFields())
                .setDuplicateStatuses(src.isDuplicateStatuses())
        ;
    }
}
