package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.AttachmentSnapshot;
import org.twins.core.dao.twin.TwinAttachmentEntity;

import java.util.HashMap;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class HistoryContextAttachment extends HistoryContext {
    public static final String DISCRIMINATOR = "history.attachment";
    private AttachmentSnapshot attachment;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        AttachmentSnapshot.extractTemplateVars(vars, attachment, "attachment");
        return vars;
    }

    public HistoryContextAttachment shotAttachment(TwinAttachmentEntity attachmentEntity) {
        attachment = AttachmentSnapshot.convertEntity(attachmentEntity);
        return this;
    }
}
