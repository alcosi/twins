package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryBranchMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryBranchMode.class)
public class FactoryBranchRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryBranchEntity, FactoryBranchDTOv1> {

    @Override
    public void map(TwinFactoryBranchEntity src, FactoryBranchDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryBranchMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionSetInvert(src.getTwinFactoryConditionInvert())
                        .setNextFactoryId(src.getNextTwinFactoryId())
                        .setDescription(src.getDescription())
                        .setActive(src.getActive());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId());

        }
    }
}
