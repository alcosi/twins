package org.twins.core.domain.face;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.face.FaceTwidgetEntity;
import org.twins.core.dao.twin.TwinEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwidgetConfig<T extends FaceTwidgetEntity> {
    private UUID targetTwinId;  // this field can be used to get some twin sift
    private TwinEntity targetTwin;
    private T config;
}
