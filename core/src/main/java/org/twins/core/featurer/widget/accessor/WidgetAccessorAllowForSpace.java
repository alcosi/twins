package org.twins.core.featurer.widget.accessor;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Properties;

@Component
@Featurer(id = 1403,
        name = "WidgetAccessorAllowForSpace",
        description = "")
public class WidgetAccessorAllowForSpace extends WidgetAccessor {
    @Override
    protected boolean isAvailableForClass(Properties properties, TwinClassEntity twinClassEntity) {
        return twinClassEntity.isSpace();
    }
}
