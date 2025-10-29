package org.twins.core.mappers.rest.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.system.MemoryInfo;
import org.twins.core.dto.rest.system.MemoryInfoDTO;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class MemoryInfoDTOMapper extends RestSimpleDTOMapper<MemoryInfo, MemoryInfoDTO> {

    @Override
    public void map(MemoryInfo src, MemoryInfoDTO dst, MapperContext mapperContext) throws Exception {
        dst
                .setName(src.getName())
                .setMax(src.getMax())
                .setUsed(src.getUsed())
                .setCommitted(src.getCommitted());
    }
}
