package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAttachmentEntity;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class AttachmentAUD {
    private List<TwinAttachmentEntity> addEntityList;
    private List<UUID> deleteUUIDList;
    private List<TwinAttachmentEntity> updateEntityList;
}
