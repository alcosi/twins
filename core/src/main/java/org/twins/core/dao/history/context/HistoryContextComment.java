package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.CommentSnapshot;
import org.twins.core.dao.comment.TwinCommentEntity;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class HistoryContextComment extends HistoryContext {
    public static final String DISCRIMINATOR = "history.comment";
    private CommentSnapshot comment;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        CommentSnapshot.extractTemplateVars(vars, comment, "comment");
        return vars;
    }

    @Override
    public String templateFromValue() {
        return null;
    }

    @Override
    public String templateToValue() {
        return null;
    }

    public HistoryContextComment shotComment(TwinCommentEntity commentEntity) {
        comment = CommentSnapshot.convertEntity(commentEntity);
        return this;
    }
}
