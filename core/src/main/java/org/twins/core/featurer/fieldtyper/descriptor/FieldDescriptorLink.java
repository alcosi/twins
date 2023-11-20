package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorLink extends FieldDescriptor {
    private boolean multiple;
    private UUID linkId;
    private List<TwinEntity> dstTwins = new ArrayList<>();

    public FieldDescriptorLink add(TwinEntity dstTwin) {
        dstTwins.add(dstTwin);
        return this;
    }
}
