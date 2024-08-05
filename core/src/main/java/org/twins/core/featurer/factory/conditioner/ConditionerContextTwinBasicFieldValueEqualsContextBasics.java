package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2420,
        name = "ConditionerContextTwinBasicFieldValueEqualsContextBasics",
        description = "")
@Slf4j
public class ConditionerContextTwinBasicFieldValueEqualsContextBasics extends Conditioner {

    private static final String assignerUserId = "assignee";
    private static final String createdByUserId = "creator";
    private static final String name = "name";
    private static final String description = "description";

    @FeaturerParam(name = "fields", description = "List of basic fields to check(divider ,)")
    public static final FeaturerParamString fields = new FeaturerParamString("fields");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        List<String> fieldsList = List.of(fields.extract(properties).split(","));
        if(null != factoryItem.getFactoryContext()) {
            TwinBasicFields basics = factoryItem.getFactoryContext().getBasics();
            if (!fieldsList.isEmpty() && null != basics) {
                for (String field : fieldsList) {
                    switch (field) {
                        case createdByUserId:
                            if(null != basics.getCreatedByUserId())
                                if(!basics.getCreatedByUserId().equals(factoryItem.checkSingleContextTwin().getCreatedByUserId()))
                                    return false;
                            break;
                        case assignerUserId:
                            if(null != basics.getAssignerUserId())
                                if(!basics.getAssignerUserId().equals(factoryItem.checkSingleContextTwin().getAssignerUserId()))
                                    return false;
                            break;
                        case name:
                            if(null != basics.getName())
                                if(!basics.getName().equals(factoryItem.checkSingleContextTwin().getName()))
                                    return false;
                            break;
                        case description:
                            if(null != basics.getDescription())
                                if(!basics.getDescription().equals(factoryItem.checkSingleContextTwin().getDescription()))
                                    return false;
                            break;
                        default:
                            throw new ServiceException(ErrorCodeTwins.TWIN_BASIC_FIELD_UNKNOWN, "Unknown twin basic field: " + field);
                    }
                }
            }
        }
        return true;
    }
}
