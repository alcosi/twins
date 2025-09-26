package org.twins.core.domain.projection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ProjectionUpdate extends ProjectionSave {
    private UUID id;
}
