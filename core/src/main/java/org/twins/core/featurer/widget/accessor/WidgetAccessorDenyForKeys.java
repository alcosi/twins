package org.twins.core.featurer.widget.accessor;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1404,
        name = "DenyForKeys",
        description = "")
public class WidgetAccessorDenyForKeys extends WidgetAccessor {
    @FeaturerParam(name = "Twin class id list", description = "", order = 1)
    public static final FeaturerParamUUIDSet twinClassIdList = new FeaturerParamUUIDSetTwinsClassId("twinClassIdList");

    @Override
    protected boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity) {
        return !twinClassIdList.extract(properties).contains(twinClassEntity.getId());
    }
}
