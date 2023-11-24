package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinUpdate {
    private TwinEntity dbTwinEntity;
    private TwinEntity updatedEntity;
    private List<FieldValue> updatedFields;
    private EntityCUD<TwinAttachmentEntity> attachmentCUD;
    private EntityCUD<TwinLinkEntity> twinLinkCUD;
}
