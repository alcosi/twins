package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinClassFreezeUpdate extends TwinClassFreezeSave {
    private UUID id;
}
