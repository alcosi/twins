package org.twins.core.dao.face;

import java.util.UUID;

public interface FacePointedEntity extends FaceVariant {
    UUID getTargetTwinFacePointerId();
    UUID getFaceId();
    FaceEntity getFace();
}
