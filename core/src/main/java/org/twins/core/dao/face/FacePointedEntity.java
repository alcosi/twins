package org.twins.core.dao.face;

import java.util.UUID;

public interface FacePointedEntity extends FaceVariantEntity {
    UUID getTargetTwinPointerId();
    UUID getFaceId();
    FaceEntity getFace();
}
