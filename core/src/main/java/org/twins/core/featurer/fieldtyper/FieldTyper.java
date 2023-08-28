package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;


@FeaturerType(id = 13,
        name = "FieldTyper",
        description = "Customize format of twin class field")
@Slf4j
public abstract class FieldTyper extends Featurer {
    public abstract String getType();

    public Hashtable<String, String> getUiParamList(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties listerProperties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<String, Object>());
        return getUiParamList(listerProperties);
    }

    protected abstract Hashtable<String, String> getUiParamList(Properties propertiess);
}
