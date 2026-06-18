package org.twins.core.domain.twinflow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TransitionUpdate extends TransitionSave {
    private UUID id;
}
