package org.twins.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.cambium.common.util.UuidUtils.NULLIFY_MARKER;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinUpdate extends TwinOperation {

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
        return !(dbTwinEntity.equals(getTwinEntity()) &&
                attachmentCUD.isEmpty() &&
                twinLinkCUD.isEmpty() &&
                fields.isEmpty() &&
                tagsDelete.isEmpty() &&
                markersDelete.isEmpty() &&
                markersAdd.isEmpty() &&
                newTags.isEmpty() &&
                existingTags.isEmpty());
    }

}
