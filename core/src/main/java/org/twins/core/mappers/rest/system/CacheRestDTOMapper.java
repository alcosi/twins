package org.twins.core.mappers.rest.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.system.CacheInfo;
import org.twins.core.dto.rest.system.CacheRsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class CacheRestDTOMapper extends RestSimpleDTOMapper<CacheInfo, CacheRsDTOv1> {

    @Override
    public void map(CacheInfo src, CacheRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setItemsCount(src.getItemsCount())
                .setSizeInMb(src.getSizeInMb());
    }
}
