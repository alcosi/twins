package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinLinkAUD {
    private List<TwinLinkEntity> addEntityList;
    private List<UUID> deleteUUIDList;
    private List<TwinLinkEntity> updateEntityList;
}
