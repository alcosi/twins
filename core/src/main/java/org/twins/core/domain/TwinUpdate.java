package org.twins.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinUpdate extends TwinOperation {
    private TwinEntity dbTwinEntity; // entity loaded from db without changes
    private EntityCUD<TwinAttachmentEntity> attachmentCUD;
    private EntityCUD<TwinLinkEntity> twinLinkCUD;
}
