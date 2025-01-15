package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryBranchUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchUpdateRqDTOv1, TwinFactoryBranchEntity> {

    private final FactoryBranchSaveDTOReverseMapper factoryBranchSaveDTOReverseMapper;

    @Override
    public void map(FactoryBranchUpdateRqDTOv1 src, TwinFactoryBranchEntity dst, MapperContext mapperContext) {
        factoryBranchSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
