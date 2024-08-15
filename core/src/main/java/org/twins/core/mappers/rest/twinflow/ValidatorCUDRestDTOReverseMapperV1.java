package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionValidatorEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.twinflow.ValidatorCudDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinflow.TwinflowTransitionValidatorService;


@Component
@RequiredArgsConstructor
public class ValidatorCUDRestDTOReverseMapperV1 extends RestSimpleDTOMapper<ValidatorCudDTOv1, EntityCUD<TwinflowTransitionValidatorEntity>> {
    private final ValidatorCreateRestDTOReverseMapper validatorCreateRestDTOReverseMapper;
    private final ValidatorUpdateRestDTOReverseMapper validatorUpdateRestDTOReverseMapper;
    private final TwinflowTransitionValidatorService twinflowTransitionValidatorService;

    @Override
    public void map(ValidatorCudDTOv1 src, EntityCUD<TwinflowTransitionValidatorEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(validatorUpdateRestDTOReverseMapper.convertCollection(src.getUpdate()))
                .setCreateList(validatorCreateRestDTOReverseMapper.convertCollection(src.getCreate()))
                .setDeleteList(twinflowTransitionValidatorService.findEntitiesSafe(src.getDelete()).getList());
    }
}
