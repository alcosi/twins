package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryBranchRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryBranchEntity, FactoryBranchDTOv2> {

    private final FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;



    @Override
    public void map(TwinFactoryBranchEntity src, FactoryBranchDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryBranchRestDTOMapper.map(src, dst, mapperContext);

    }
}
