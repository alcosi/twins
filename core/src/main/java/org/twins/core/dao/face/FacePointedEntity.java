package org.twins.core.dao.face;

import java.util.UUID;

public interface FacePointedEntity extends FaceVariantEntity {
    UUID getTargetTwinPointerId();
    UUID getFaceId();
    FaceEntity getFace();
    /**
     * Covariant signature: entities use {@code @Accessors(chain = true)}, so the Lombok-generated
     * setter returns the concrete entity type — a valid override of this method.
     */
    FacePointedEntity setFace(FaceEntity face);
}
