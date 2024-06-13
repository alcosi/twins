package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassUpdate {
    private TwinClassEntity updateTwinClassEntity;
    private TwinClassEntity dbTwinClassEntity;
    private Map<UUID, UUID> markersRemap;
}
