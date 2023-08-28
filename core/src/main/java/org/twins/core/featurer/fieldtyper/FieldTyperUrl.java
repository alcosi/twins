package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Properties;

@Component
@Featurer(id = 1303,
        name = "FieldTyperUrl",
        description = "")
public class FieldTyperUrl extends FieldTyper {
    @Override
    public String getType() {
        return "url";
    }

    @Override
    public Hashtable<String, String> getUiParamList(Properties properties) {
        return null;
    }
}
