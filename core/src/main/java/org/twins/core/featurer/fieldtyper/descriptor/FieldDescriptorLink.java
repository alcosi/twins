package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorLink extends FieldDescriptor {
    private boolean multiple;
    private UUID linkId;
    private Kit<TwinEntity, UUID> dstTwins = new Kit<>(TwinEntity::getId);
}
