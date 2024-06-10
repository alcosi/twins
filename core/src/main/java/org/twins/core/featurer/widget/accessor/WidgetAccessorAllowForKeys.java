package org.twins.core.featurer.widget.accessor;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;

import java.util.Properties;

@Component
@Featurer(id = 1402,
        name = "WidgetAccessorAllowForKeys",
        description = "")
public class WidgetAccessorAllowForKeys extends WidgetAccessor {
    @FeaturerParam(name = "twinClassIdList", description = "")
    public static final FeaturerParamUUIDSet twinClassIdList = new FeaturerParamUUIDSetTwinsClassId("twinClassIdList");

    @Override
    protected boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity) {
        return twinClassIdList.extract(properties).contains(twinClassEntity.getId());
    }
}
