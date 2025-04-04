package org.twins.core.domain.motion;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclassfield.TwinClassFieldMotionEntity;

@Data
@Accessors(chain = true)
public class FieldMotionContext {
    private TwinClassFieldMotionEntity fieldMotionEntity;
    private TwinEntity twinEntity;
}
