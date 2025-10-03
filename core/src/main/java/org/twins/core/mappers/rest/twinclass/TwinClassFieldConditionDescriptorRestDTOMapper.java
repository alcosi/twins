package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDescriptorBasicDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDescriptorDTO;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptor;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorBasic;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldConditionDescriptorRestDTOMapper extends RestSimpleDTOMapper<ConditionDescriptor, TwinClassFieldConditionDescriptorDTO> {
    @Override
    public void map(ConditionDescriptor src, TwinClassFieldConditionDescriptorDTO dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinClassFieldConditionDescriptorDTO convert(ConditionDescriptor conditionDescriptor, MapperContext mapperContext) throws Exception {
        if (conditionDescriptor == null)
            return null;
        if (conditionDescriptor instanceof ConditionDescriptorBasic basic) {
            return new TwinClassFieldConditionDescriptorBasicDTOv1()
                    .conditionElement(basic.conditionElement())
                    .valueToCompareWith(basic.valueToCompareWith())
                    .evaluatedParamKey(basic.evaluatedParamKey());
        }
        return null;
    }
}
