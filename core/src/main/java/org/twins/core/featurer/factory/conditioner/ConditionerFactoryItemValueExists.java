package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookuperNearest;
import org.twins.core.featurer.params.FeaturerParamStringTwinsFactoryFieldLookuper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2453,
        name = "Factory item field value exists",
        description = "")
@Slf4j
public class ConditionerFactoryItemValueExists extends Conditioner {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Field lookupper", description = "Field lookupper", order = 2, defaultValue = "fromItemOutputFields", optional = true)
    public static final FeaturerParamStringTwinsFactoryFieldLookuper fieldLookupper = new FeaturerParamStringTwinsFactoryFieldLookuper("fieldLookupper");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return check(properties, factoryItem, ((FieldLookuperNearest) fieldLookupers.getByType(fieldLookupper.extract(properties))));
    }

    public boolean check(Properties properties, FactoryItem factoryItem, FieldLookuperNearest fieldLookuper) throws ServiceException {
        try {
            var value = fieldLookuper.lookupFieldValue(factoryItem, twinClassFieldId.extract(properties));
            return value != null && !value.isUndefined();
        } catch (ServiceException e) {
            return false;
        }
    }
}
