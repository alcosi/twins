package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.Properties;

@Component
@Featurer(id = 1305,
        name = "FieldTyperList",
        description = "")
public class FieldTyperList extends FieldTyper {
    @FeaturerParam(name = "listUUID", description = "")
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUID("listUUID");

    @FeaturerParam(name = "supportCustom", description = "If true, then user can enter custom value")
    public static final FeaturerParamBoolean supportCustom = new FeaturerParamBoolean("supportCustom");

    @Override
    public String getType() {
        return "list";
    }

    @Override
    public Hashtable<String, String> getUiParamList(Properties properties) {
        Hashtable<String, String> params = new Hashtable<>();
        params.put("listId", listUUID.extract(properties).toString());
        params.put("supportCustom", supportCustom.extract(properties).toString());
        return params;
    }
}
