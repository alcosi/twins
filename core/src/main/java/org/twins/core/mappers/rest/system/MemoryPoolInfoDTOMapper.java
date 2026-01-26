package org.twins.core.mappers.rest.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.system.MemoryPoolInfo;
import org.twins.core.dto.rest.system.MemoryPoolInfoDTO;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class MemoryPoolInfoDTOMapper extends RestSimpleDTOMapper<MemoryPoolInfo, MemoryPoolInfoDTO> {

    @Override
    public void map(MemoryPoolInfo src, MemoryPoolInfoDTO dst, MapperContext mapperContext) throws Exception {
        dst
                .setUsed(src.getUsed())
                .setName(src.getName())
                .setType(src.getType());
    }
}
