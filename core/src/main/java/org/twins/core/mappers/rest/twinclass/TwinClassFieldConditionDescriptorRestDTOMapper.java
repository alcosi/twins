package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDescriptorDTO;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditionDescriptorParamDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldConditonDescriptorValueDTOv1;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptor;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorParam;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptorValue;
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
        if (conditionDescriptor instanceof ConditionDescriptorValue descriptor) {
            return new TwinClassFieldConditonDescriptorValueDTOv1()
                    .valueToCompareWith(descriptor.valueToCompareWith())
                    .conditionOperator(descriptor.conditionOperator());
        }
        if (conditionDescriptor instanceof ConditionDescriptorParam descriptor) {
            return new TwinClassFieldConditionDescriptorParamDTOv1()
                    .evaluatedParamKey(descriptor.evaluatedParamKey())
                    .valueToCompareWith(descriptor.valueToCompareWith())
                    .conditionOperator(descriptor.conditionOperator());
        }
        return null;
    }
}
