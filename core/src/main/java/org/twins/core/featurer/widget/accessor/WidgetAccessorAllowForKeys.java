package org.twins.core.featurer.widget.accessor;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamWordList;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Properties;

@Component
@Featurer(id = 1402,
        name = "WidgetAccessorAllowForKeys",
        description = "")
public class WidgetAccessorAllowForKeys extends WidgetAccessor {
    @FeaturerParam(name = "twinClassKeyList", description = "")
    public static final FeaturerParamWordList twinKeyList = new FeaturerParamWordList("twinClassKeyList");

    @Override
    protected boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity) {
        return twinKeyList.extract(properties).contains(twinClassEntity.key());
    }
}
