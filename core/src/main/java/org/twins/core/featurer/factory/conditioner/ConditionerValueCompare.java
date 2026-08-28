package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamStringTwinsConditionOperatorType;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2445,
        name = "Value compare",
        description = "")
@Slf4j
public class ConditionerValueCompare extends Conditioner {
    @FeaturerParam(name = "Field lookuper", description = "Field lookuper", order = 0, optional = true, defaultValue = "fromContextFieldsAndContextTwinDbFields")
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookuper = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookuper");
    
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "ValueToCompareWith", description = "", order = 2)
    public static final FeaturerParamString valueToCompareWith = new FeaturerParamString("valueToCompareWith");

    @FeaturerParam(name = "ConditionOperator", description = "", order = 3)
    public static final FeaturerParamStringTwinsConditionOperatorType conditionOperator = new FeaturerParamStringTwinsConditionOperatorType("conditionOperator");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        FieldValue fieldValue = ((FieldLookuperNearest) fieldLookupers.getByType(fieldLookuper.extract(properties))).lookupFieldValue(factoryItem, twinClassFieldId.extract(properties));
        String actual = ConditionEvaluator.normalizeValue(fieldValue);
        return ConditionEvaluator.evaluateOperator(actual, conditionOperator.extract(properties), valueToCompareWith.extract(properties));
    }
}
