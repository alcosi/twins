package org.twins.core.featurer.widget.accessor;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1401,
        name = "WidgetAccessorAllowAny",
        description = "")
public class WidgetAccessorAllowAny extends WidgetAccessor {
    @Override
    protected boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity) {
        return true;
    }
}
