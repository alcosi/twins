package org.twins.core.mappers.rest.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.validator.cud.ValidatorCudDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class ValidatorCUDRestDTOReverseMapperV1 extends RestSimpleDTOMapper<ValidatorCudDTOv1, EntityCUD<TwinflowTransitionValidatorRuleEntity>> {

    //todo think about cud logic

    private final ValidatorCreateRestDTOReverseMapper validatorCreateRestDTOReverseMapper;
    private final ValidatorUpdateRestDTOReverseMapper validatorUpdateRestDTOReverseMapper;

    @Override
    public void map(ValidatorCudDTOv1 src, EntityCUD<TwinflowTransitionValidatorRuleEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(validatorUpdateRestDTOReverseMapper.convertCollection(src.getUpdate()))
                .setCreateList(validatorCreateRestDTOReverseMapper.convertCollection(src.getCreate()))
                .setDeleteUUIDList(src.getDelete());
    }
}
