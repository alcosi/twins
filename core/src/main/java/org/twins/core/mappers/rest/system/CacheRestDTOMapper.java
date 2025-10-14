package org.twins.core.mappers.rest.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.system.CacheInfo;
import org.twins.core.dto.rest.system.CacheInfoDTO;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class CacheRestDTOMapper extends RestSimpleDTOMapper<CacheInfo, CacheInfoDTO> {

    @Override
    public void map(CacheInfo src, CacheInfoDTO dst, MapperContext mapperContext) throws Exception {
        dst
                .setCacheName(src.getCacheName())
                .setItemsCount(src.getItemsCount())
                .setSizeInMb(src.getSizeInMb());
    }
}
