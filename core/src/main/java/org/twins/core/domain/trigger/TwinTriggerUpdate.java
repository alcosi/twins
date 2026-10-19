package org.twins.core.domain.trigger;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TwinTriggerUpdate extends TwinTriggerSave {
    private UUID id;
}
