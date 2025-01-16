package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryBranchCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchCreateRqDTOv1, TwinFactoryBranchEntity> {

    private final FactoryBranchSaveDTOReverseMapper factoryBranchSaveDTOReverseMapper;

    @Override
    public void map(FactoryBranchCreateRqDTOv1 src, TwinFactoryBranchEntity dst, MapperContext mapperContext) {
        factoryBranchSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
