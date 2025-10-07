package org.twins.core.dto.rest.related;

import org.twins.core.dto.rest.permission.PermissionDTOv1;

import java.util.UUID;

public interface ContainsRelatedObjects {
    void setRelatedObjects(RelatedObjectsDTOv1 relatedObjects);

    RelatedObjectsDTOv1 getRelatedObjects();

    default Object getRelatedObject(UUID relatedObjectId) {
        if (getRelatedObjects() != null) {
            var relatedObject = getRelatedObjects().get(PermissionDTOv1.class, relatedObjectId);
            if (relatedObject instanceof ContainsRelatedObjects containsRelatedObjects) {
                containsRelatedObjects.setRelatedObjects(getRelatedObjects());
            }
            return relatedObject;
        } else {
            return null;
        }
    }
}
