package org.twins.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinUpdate extends TwinOperation {
    public static final UUID NULLIFY_MARKER = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private TwinEntity dbTwinEntity; // entity loaded from db without changes
    private EntityCUD<TwinAttachmentEntity> attachmentCUD;
    private EntityCUD<TwinLinkEntity> twinLinkCUD;

    @Override
    public UUID nullifyUUID() {
        return NULLIFY_MARKER;
    }
}
