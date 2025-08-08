package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinClassUpdateStrategy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class TwinUpdateClass {

    private UUID twinId;
    private UUID newTwinClassId;
    private UUID newHeadTwinId;
    private Map<UUID, UUID> fieldsReplaceMap; // map [old twin class field id -> new twin class field id]
    private List<TwinClassUpdateStrategy> behavior;

}
