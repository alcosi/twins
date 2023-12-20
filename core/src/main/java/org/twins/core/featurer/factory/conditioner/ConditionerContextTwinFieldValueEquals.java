package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;

@Component
@Featurer(id = 2401,
        name = "ConditionerContextTwinFieldValueEquals",
        description = "")
@Slf4j
public class ConditionerContextTwinFieldValueEquals extends Conditioner {
    @FeaturerParam(name = "twinClassFieldId", description = "")
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUID("twinClassFieldId");

    @FeaturerParam(name = "value", description = "")
    public static final FeaturerParamString value = new FeaturerParamString("value");

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassFieldService twinClassFieldService;

    @Lazy
    @Autowired
    TwinFieldRestDTOMapperV2 twinFieldRestDTOMapperV2;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin = Filler.checkNotMultiplyContextTwin(factoryItem);
        if (contextTwin == null)
            return false;
        TwinFieldEntity srcField = twinService.findTwinField(contextTwin.getId(), twinClassFieldId.extract(properties));
        if (srcField == null) {
            log.warn("twinClassField[" + twinClassFieldId.extract(properties) + "] is not present for context " + contextTwin.logShort());
            return false;
        }
        FieldValueText fieldValue;
        try {
            fieldValue = twinFieldRestDTOMapperV2.convert(srcField);
            if (fieldValue != null && fieldValue.getValue() != null && fieldValue.getValue().equals(value.extract(properties)))
                return true;
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT);
        }
        return false;
    }
}
