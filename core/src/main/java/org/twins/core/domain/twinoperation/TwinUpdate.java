package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.CudUtils;
import org.cambium.common.util.MapUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
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
    private EntityCUD<TwinFieldAttributeEntity> twinFieldAttributeCUD;
    private boolean checkEditPermission = false;
    private Mode mode = Mode.twinUpdate; //we had to create this flag here, because a status of dbTwinEntity can be changed during TwinUpdate flow

    public TwinUpdate setDbTwinEntity(TwinEntity dbTwinEntity) {
        this.dbTwinEntity = dbTwinEntity;
        if (dbTwinEntity.isSketch())
            mode = Mode.sketchUpdate;
        return this;
    }

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
        return !Objects.equals(dbTwinEntity, getTwinEntity()) ||
                mode == Mode.sketchUpdate || // this mode helps to overcome "has changes" check logic. Sketch twin can have no direct changes, but some children dependent logic
                CollectionUtils.isNotEmpty(tagsDelete) ||
                CollectionUtils.isNotEmpty(markersDelete) ||
                CollectionUtils.isNotEmpty(markersAdd) ||
                CollectionUtils.isNotEmpty(tagsAddNew) ||
                CollectionUtils.isNotEmpty(tagsAddExisted) ||
                CudUtils.isNotEmpty(attachmentCUD) ||
                CudUtils.isNotEmpty(twinLinkCUD);
    }

    public boolean isAttachmentCUDOnly() {
        return Objects.equals(dbTwinEntity, getTwinEntity()) &&
                MapUtils.isEmpty(fields) &&
                CollectionUtils.isEmpty(tagsDelete) &&
                CollectionUtils.isEmpty(markersDelete) &&
                CollectionUtils.isEmpty(markersAdd) &&
                CollectionUtils.isEmpty(tagsAddNew) &&
                CollectionUtils.isEmpty(tagsAddExisted) &&
                CudUtils.isNotEmpty(attachmentCUD) &&
                CudUtils.isEmpty(twinLinkCUD);
    }

    public enum Mode {
        twinUpdate,
        sketchUpdate,
        sketchFinalize,
        sketchFinalizeRestricted;

        public boolean isSketch() {
            return this == sketchUpdate || this == sketchFinalize || this == sketchFinalizeRestricted;
        }
    }
}
