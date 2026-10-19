package org.twins.core.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;

import java.util.Properties;

@FeaturerParamType(
        id = "STRING:TWINS:TWIN_FACTORY_FIELD_LOOKUPPER",
        description = "twin factory field lookupper",
        regexp = FeaturerParamStringTwinsFactoryFieldLookuper.TWIN_FACTORY_LOOKUPPER_REGEXP,
        example = "fromContextFields")
public class FeaturerParamStringTwinsFactoryFieldLookuper extends FeaturerParam<FieldLookupers.Type> {
    public static final String TWIN_FACTORY_LOOKUPPER_REGEXP = "fromContextFields|fromContextFieldsAndContextTwinDbFields|fromContextTwinFields|fromContextTwinDbFields|fromContextTwinLinkedByLinkTwinFields|fromContextTwinLinkedByFieldTwinFields|fromContextTwinHeadTwinDbFields|fromContextTwinUncommitedFields|fromItemOutputDbFields|fromItemOutputUncommitedFields|fromItemOutputFields|fromItemOutputHeadTwinFields|fromItemOutputLinkedTwinFields|fromItemOutputHeadTwinLinkedTwinFields|fromItemOutputLinkedTwinHeadTwinFields";

    public FeaturerParamStringTwinsFactoryFieldLookuper(String key) {
        super(key);
    }

    @Override
    public FieldLookupers.Type extract(Properties properties) {
        String value = (String) properties.get(key);
        return value != null ?
                FieldLookupers.Type.valueOf(value) :
                FieldLookupers.Type.fromContextFields;
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(TWIN_FACTORY_LOOKUPPER_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS, "param[" + key + "] value[" + value + "] must be one of: " + TWIN_FACTORY_LOOKUPPER_REGEXP);
        }
    }
}