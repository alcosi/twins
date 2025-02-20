package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.domain.TwinBasicFields;

import java.util.*;

@FeaturerParamType(
        id = "WORD_LIST:TWINS:TWIN_BASIC_FIELD",
        description = "",
        regexp = ".*",
        example = FeaturerParamUUIDSet.UUID_SET_EXAMPLE)
public class FeaturerParamBasicsSetTwinBasicField extends FeaturerParam<Set<TwinBasicFields.Basics>> {

    public FeaturerParamBasicsSetTwinBasicField(String key) {
        super(key);
    }

    @Override
    public Set<TwinBasicFields.Basics> extract(Properties properties) {
        String value = (String) properties.get(key);
        List<String> stringList = Arrays.stream(value.split(",")).map(String::trim).toList();
        Set<TwinBasicFields.Basics> ret = new HashSet<>();
        for (String s : stringList)
            ret.add(TwinBasicFields.Basics.valueOf(s));
        return ret;
    }
}
