package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.EntityCUD;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.cambium.common.util.UuidUtils.NULLIFY_MARKER;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinUpdate extends TwinSave {
    private TwinEntity dbTwinEntity; // entity loaded from db without changes
    private EntityCUD<TwinAttachmentEntity> attachmentCUD;
    private EntityCUD<TwinLinkEntity> twinLinkCUD;
    protected Set<UUID> markersDelete;
    protected Set<UUID> tagsDelete;

    @Override
    public UUID nullifyUUID() {
        return NULLIFY_MARKER;
    }

    public TwinOperation deleteMarker(UUID marker) {
        if (markersDelete == null)
            markersDelete = new HashSet<>();
        markersDelete.add(marker);
        return this;
    }

    public boolean isChanged() {
        return !(
                Objects.equals(dbTwinEntity, getTwinEntity()) &&
                        (null == attachmentCUD || attachmentCUD.isEmpty()) &&
                        (null == twinLinkCUD || twinLinkCUD.isEmpty()) &&
                        (null == fields || fields.isEmpty()) &&
                        (null == tagsDelete || tagsDelete.isEmpty()) &&
                        (null == markersDelete || markersDelete.isEmpty()) &&
                        (null == markersAdd || markersAdd.isEmpty()) &&
                        (null == tagsAddNew || tagsAddNew.isEmpty()) &&
                        (null == tagsAddExisted || tagsAddExisted.isEmpty())
        );
    }

}
