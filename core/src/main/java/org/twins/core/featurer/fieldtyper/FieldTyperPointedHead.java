package org.twins.core.featurer.fieldtyper;


import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStoragePointedHead;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.HashMap;
import java.util.Properties;


@Component
@Featurer(id = FeaturerTwins.ID_1349,
        name = "Pointed field from head",
        description = "")
public class FieldTyperPointedHead extends FieldTyper<FieldDescriptor, FieldValue, TwinFieldStoragePointedHead, TwinFieldSearchNotImplemented> {
    @FeaturerParam(name = "Head twin field", description = "", order = 3)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUID("headTwinClassFieldId");

    @Lazy
    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    public Class<FieldValue> getValueType(TwinClassFieldEntity twinClassField) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassField.getFieldTyperParams(), new HashMap<>());
        var headTwinClassField = getHeadTwinClassFieldSafe(twinClassField, properties);
        var headFieldTyper = featurerService.getFeaturer(headTwinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
        return headFieldTyper.getValueType(headTwinClassField); //perhaps we should some wrapper here to indicate that the current value is just a pointer
    }

    @Override
    protected FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        var headTwinClassField = getHeadTwinClassFieldSafe(twinClassFieldEntity, properties);
        var headFieldTyper = featurerService.getFeaturer(headTwinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
        // this is recursion safe, even if some head pointed field points on another head pointed field, this will just head of head up ()
        var fieldDescriptor = headFieldTyper.getFieldDescriptor(headTwinClassField);
        //todo set editable false
        return fieldDescriptor;
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValue value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValue deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var headTwinClassField = getHeadTwinClassFieldSafe(twinField.getTwinClassField(), properties);
        return twinField.getTwin().getHeadTwin().getFieldValuesKit().get(headTwinClassField.getId());
    }

    protected TwinClassFieldEntity getHeadTwinClassFieldSafe(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        var headTwinClassField = twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties));
        if (!headTwinClassField.getTwinClassId().equals(twinClassFieldEntity.getTwinClass().getHeadTwinClassId())) {
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID, headTwinClassField.logNormal() + " is not of " + twinClassFieldEntity.getTwinClass().logNormal());
        }
        return headTwinClassField;
    }

    protected FieldTyper getHeadFieldTyperSafe(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        var headTwinClassField = getHeadTwinClassFieldSafe(twinClassFieldEntity, properties);
        return featurerService.getFeaturer(headTwinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
    }
}
