package org.twins.core.dto.rest.related;

import org.twins.core.dto.rest.permission.PermissionDTOv1;

import java.util.UUID;

public interface ContainsRelatedObjects {
    void setRelatedObjects(RelatedObjectsDTOv1 relatedObjects);

    RelatedObjectsDTOv1 getRelatedObjects();

    default Object getRelatedObject(Class<?> relatedObjectClass, UUID relatedObjectId) {
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

    default Object getRelatedObject(Class<?> relatedObjectClass, int featurerId) {
        if (getRelatedObjects() != null) {
            return getRelatedObjects().getFeaturerMap().get(featurerId);
        } else {
            return null;
        }
    }
}
