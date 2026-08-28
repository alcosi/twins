package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamBasicsTwinBasicField;

import java.util.Objects;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2422,
        name = "Twin basic field value equals context basics",
        description = "")
@Slf4j
public class ConditionerContextTwinBasicFieldValueEqualsContextBasics extends Conditioner {

    @FeaturerParam(name = "Field", description = "Basic field to check", order = 1)
    public static final FeaturerParamBasicsTwinBasicField field = new FeaturerParamBasicsTwinBasicField("field");

    @FeaturerParam(name = "Resolve from factory item", description = "Compare factory item twin basic field, else context twin", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean factoryItemElseContext = new FeaturerParamBoolean("factoryItemElseContext");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        if (null == factoryItem.getFactoryContext())
            return false;
        TwinBasicFields basics = factoryItem.getFactoryContext().getBasics();
        TwinBasicFields.Basics fieldName = field.extract(properties);
        if (null == basics)
            return false;
        TwinEntity contextTwin;
        if (factoryItemElseContext.extract(properties)) {
            contextTwin = factoryItem.getTwin();
        } else {
            contextTwin = factoryItem.checkSingleContextTwin();
        }
        if (null == contextTwin)
            return false;
        switch (fieldName) {
            case createdByUserId:
                if (Objects.equals(basics.getCreatedByUserId(), contextTwin.getCreatedByUserId())) return true;
                break;
            case assigneeUserId:
                if (Objects.equals(basics.getAssigneeUserId(), contextTwin.getAssignerUserId())) return true;
                break;
            case name:
                if (Objects.equals(basics.getName(), contextTwin.getName())) return true;
                break;
            case description:
                if (Objects.equals(basics.getDescription(), contextTwin.getDescription())) return true;
                break;
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_BASIC_FIELD_UNKNOWN, "Unknown contextTwin basic field: " + fieldName);
        }
        return false;
    }
}
