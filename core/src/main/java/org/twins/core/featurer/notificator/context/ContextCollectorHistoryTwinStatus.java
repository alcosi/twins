package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextStatusChange;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4905,
        name = "Context collector twin status from history",
        description = "Collect form twin (status)")
@Slf4j
public class ContextCollectorHistoryTwinStatus extends ContextCollector {

    @FeaturerParam(name = "Collect src status", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectSrcStatus = new FeaturerParamBoolean("collectSrcStatus");

    @FeaturerParam(name = "Collect src status name", description = "", order = 2, optional = true, defaultValue = "TWIN_SRC_STATUS_NAME")
    public static final FeaturerParamString collectSrcStatusKey = new FeaturerParamString("collectSrcStatusKey");

    @FeaturerParam(name = "Collect dst status", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectDstStatus = new FeaturerParamBoolean("collectDstStatus");

    @FeaturerParam(name = "Collect src status name", description = "", order = 4, optional = true, defaultValue = "TWIN_DST_STATUS_NAME")
    public static final FeaturerParamString collectDstStatusKey = new FeaturerParamString("collectDstStatusKey");

    @FeaturerParam(name = "Collect src status background color", description = "", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectSrcStatusBackgroundColor = new FeaturerParamBoolean("collectSrcStatusBackgroundColor");

    @FeaturerParam(name = "Collect src status background color key", description = "", order = 6, optional = true, defaultValue = "TWIN_SRC_STATUS_BACKGROUND_COLOR")
    public static final FeaturerParamString collectSrcStatusBackgroundColorKey = new FeaturerParamString("collectSrcStatusBackgroundColorKey");

    @FeaturerParam(name = "Collect src status font color", description = "", order = 7, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectSrcStatusFontColor = new FeaturerParamBoolean("collectSrcStatusFontColor");

    @FeaturerParam(name = "Collect src status font color key", description = "", order = 8, optional = true, defaultValue = "TWIN_SRC_STATUS_FONT_COLOR")
    public static final FeaturerParamString collectSrcStatusFontColorKey = new FeaturerParamString("collectSrcStatusFontColorKey");

    @FeaturerParam(name = "Collect dst status background color", description = "", order = 9, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectDstStatusBackgroundColor = new FeaturerParamBoolean("collectDstStatusBackgroundColor");

    @FeaturerParam(name = "Collect dst status background color key", description = "", order = 10, optional = true, defaultValue = "TWIN_DST_STATUS_BACKGROUND_COLOR")
    public static final FeaturerParamString collectDstStatusBackgroundColorKey = new FeaturerParamString("collectDstStatusBackgroundColorKey");

    @FeaturerParam(name = "Collect dst status font color", description = "", order = 11, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectDstStatusFontColor = new FeaturerParamBoolean("collectDstStatusFontColor");

    @FeaturerParam(name = "Collect dst status font color key", description = "", order = 12, optional = true, defaultValue = "TWIN_DST_STATUS_FONT_COLOR")
    public static final FeaturerParamString collectDstStatusFontColorKey = new FeaturerParamString("collectDstStatusFontColorKey");

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) {
        HistoryContextStatusChange statusChange = (HistoryContextStatusChange) history.getContext();
        if (collectSrcStatus.extract(properties)) {
            context.put(collectSrcStatusKey.extract(properties), statusChange.getFromStatus().getName());
        }
        if (collectSrcStatusBackgroundColor.extract(properties)) {
            context.put(collectSrcStatusBackgroundColorKey.extract(properties), statusChange.getFromStatus().getBackgroundColor());
        }
        if (collectSrcStatusFontColor.extract(properties)) {
            context.put(collectSrcStatusFontColorKey.extract(properties), statusChange.getFromStatus().getFontColor());
        }
        if (collectDstStatus.extract(properties)) {
            context.put(collectDstStatusKey.extract(properties), statusChange.getToStatus().getName());
        }
        if (collectDstStatusBackgroundColor.extract(properties)) {
            context.put(collectDstStatusBackgroundColorKey.extract(properties), statusChange.getToStatus().getBackgroundColor());
        }
        if (collectDstStatusFontColor.extract(properties)) {
            context.put(collectDstStatusFontColorKey.extract(properties), statusChange.getToStatus().getFontColor());
        }
        return context;
    }
}
