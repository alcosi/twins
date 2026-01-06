package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.history.context.HistoryContextComment;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4906,
        name = "Context collector comment from history",
        description = "Collect form twin (status)")
@Slf4j
public class ContextCollectorHistoryComment extends ContextCollector {

    @FeaturerParam(name = "Collect message from comment", description = "", order = 1, optional = true, defaultValue = "COMMENT")
    public static final FeaturerParamString collectCommentKey = new FeaturerParamString("collectCommentKey");

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) {
        HistoryContextComment comment = (HistoryContextComment) history.getContext();
        context.put(collectCommentKey.extract(properties), comment.getComment().getText());
        return context;
    }
}
