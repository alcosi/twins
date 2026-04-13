package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFieldDuplicate;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TwinClassFieldDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldDuplicateDTOv1, TwinClassFieldDuplicate> {

    @Override
    public void map(TwinClassFieldDuplicateDTOv1 src, TwinClassFieldDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalTwinClassFieldId(src.getOriginalTwinClassFieldId())
                .setNewTwinClassId(src.getNewTwinClassId())
                .setNewKey(src.getNewKey())
                .setDuplicateRules(src.isDuplicateRules())
        ;
    }
}
