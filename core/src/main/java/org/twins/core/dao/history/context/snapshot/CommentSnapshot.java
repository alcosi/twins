package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.comment.TwinCommentEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class CommentSnapshot {
    private UUID id;
    private String text;

    public static CommentSnapshot convertEntity(TwinCommentEntity commentEntity) {
        if (commentEntity == null)
            return null;
        return new CommentSnapshot()
                .setId(commentEntity.getId())
                .setText(commentEntity.getText());
    }

    public static void extractTemplateVars(HashMap<String, String> vars, CommentSnapshot commentSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", commentSnapshot != null ? commentSnapshot.id.toString() : "");
        vars.put(prefix + "text", commentSnapshot != null ? commentSnapshot.text : "");
    }
}
