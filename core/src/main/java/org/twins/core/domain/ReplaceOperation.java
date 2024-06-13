package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class ReplaceOperation {
    public Strategy strategy = Strategy.deleteIfMissed;
    public Map<UUID, UUID> replaceMap;

    public enum Strategy {
        deleteIfMissed,
        restrictIfMissed
    }
}
