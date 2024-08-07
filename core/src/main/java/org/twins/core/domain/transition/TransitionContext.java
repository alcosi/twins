package org.twins.core.domain.transition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.TwinCreate;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TransitionContext extends TransitionDraftContext {
    private Map<UUID, FieldValue> fields; // key: twinClassFieldId
    private List<TwinCreate> newTwinList;
    private EntityCUD<TwinAttachmentEntity> attachmentCUD;
    private EntityCUD<TwinLinkEntity> twinLinkCUD;
    private TwinBasicFields basics;

    public boolean isSimple() {
        return MapUtils.isEmpty(fields)
                && CollectionUtils.isEmpty(newTwinList)
                && (attachmentCUD == null || attachmentCUD.isEmpty())
                && (twinLinkCUD == null || twinLinkCUD.isEmpty());
    }

    public TransitionContext setFields(List<FieldValue> fieldValueList) {
        if (fieldValueList != null) {
            fields = new HashMap<>();
            for (FieldValue fieldValue : fieldValueList)
                fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        }
        return this;
    }
}
