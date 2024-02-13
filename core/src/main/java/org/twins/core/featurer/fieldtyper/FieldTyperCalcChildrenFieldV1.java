package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinFieldSpecification;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinFieldRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorCalcChildrenField;
import org.twins.core.featurer.fieldtyper.value.FieldValueCalcChildrenField;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = 1312,
        name = "FieldTyperCalcChildrenFieldV1",
        description = "")
public class FieldTyperCalcChildrenFieldV1 extends FieldTyper<FieldDescriptorCalcChildrenField, FieldValueCalcChildrenField> {


    @Autowired
    TwinFieldRepository twinFieldRepository;

    @FeaturerParam(name = "childrenTwinClassFieldId", description = "")
    public static final FeaturerParamUUID childrenTwinClassFieldId = new FeaturerParamUUID("childrenTwinClassFieldId");

    @FeaturerParam(name = "includeChildrenTwinStatusIdList", description = "")
    public static final FeaturerParamUUIDSet includeChildrenTwinStatusIdList = new FeaturerParamUUIDSet("includeChildrenTwinStatusIdList");


    @Deprecated
    @Override
    public FieldDescriptorCalcChildrenField getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorCalcChildrenField();
    }

    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueCalcChildrenField value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueCalcChildrenField deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) throws ServiceException {
        Double result = 0d;;
        List<TwinFieldEntity> resultTwinFieldsList = twinFieldRepository.findAll(
                TwinFieldSpecification.getCalcChildrenFieldSpecification(
                        twinFieldEntity, true, childrenTwinClassFieldId.extract(properties), includeChildrenTwinStatusIdList.extract(properties)
                )
        );
        if (!resultTwinFieldsList.isEmpty()) {
            for (TwinFieldEntity item : resultTwinFieldsList) {
                try {
                    result += Double.parseDouble(item.getValue());
                } catch (NumberFormatException e) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " value[" + item.getValue() + "] cant be parsed to Double");
                }
            }
        }
        return new FieldValueCalcChildrenField().setValue(result);
    }
}
