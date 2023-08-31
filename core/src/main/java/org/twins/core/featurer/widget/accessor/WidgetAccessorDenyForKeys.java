package org.twins.core.featurer.widget.accessor;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamWordList;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Properties;

@Component
@Featurer(id = 1404,
        name = "WidgetAccessorDenyForKeys",
        description = "")
public class WidgetAccessorDenyForKeys extends WidgetAccessor {
    @FeaturerParam(name = "twinClassKeyList", description = "")
    public static final FeaturerParamWordList twinKeyList = new FeaturerParamWordList("twinClassKeyList");

    @Override
    protected boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity) {
        return !twinKeyList.extract(properties).contains(twinClassEntity.key());
    }
}
