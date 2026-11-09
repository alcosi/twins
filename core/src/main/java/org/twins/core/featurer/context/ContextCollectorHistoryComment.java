package org.twins.core.featurer.context;

import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.context.HistoryContextComment;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4906,
        name = "Context collector comment from history",
        description = "Collect form twin (status)")
public class ContextCollectorHistoryComment extends ContextCollectorAtomic<ContextRootHistory> {

    @FeaturerParam(name = "Collect message from comment", description = "", order = 1, optional = true, defaultValue = "COMMENT")
    public static final FeaturerParamString collectCommentKey = new FeaturerParamString("collectCommentKey");

    @Override
    protected Map<String, String> collectData(ContextRootHistory root, Map<String, String> context, Properties properties) {
        HistoryContextComment comment = (HistoryContextComment) root.getHistory().getContext();
        context.put(collectCommentKey.extract(properties), comment.getComment().getText());
        return context;
    }
}
