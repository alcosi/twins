package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldPlugEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldPlugBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldPlugRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldPlugBaseDTOv1, TwinClassFieldPlugEntity> {

    @Override
    public void map(TwinClassFieldPlugBaseDTOv1 src, TwinClassFieldPlugEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setTwinClassFieldId(src.getTwinClassFieldId());
    }
}
