package org.twins.core.dto.rest.related;

import org.cambium.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ContainsRelatedObjects {

    void setRelatedObjects(RelatedObjectsDTOv1 relatedObjects);

    RelatedObjectsDTOv1 getRelatedObjects();

    default <T> T getRelatedObject(Class<T> relatedObjectClass, Object relatedObjectId) {
        if (getRelatedObjects() != null && relatedObjectId != null) {
            var relatedObject = getRelatedObjects().get(relatedObjectClass, relatedObjectId);
            if (relatedObject instanceof ContainsRelatedObjects containsRelatedObjects) {
                containsRelatedObjects.setRelatedObjects(getRelatedObjects());
            }
            return relatedObject;
        } else {
            return null;
        }
    }

    default <T> List<T> getRelatedObjectList(Class<T> relatedObjectClass, Collection<?> relatedObjectIdList) {
        if (getRelatedObjects() != null && CollectionUtils.isNotEmpty(relatedObjectIdList)) {
            List<T> ret = new ArrayList<>();
            for (var relatedObjectId : relatedObjectIdList) {
                var relatedObject = getRelatedObjects().get(relatedObjectClass, relatedObjectId);
                if (relatedObject instanceof ContainsRelatedObjects containsRelatedObjects) {
                    containsRelatedObjects.setRelatedObjects(getRelatedObjects());
                }
                ret.add(relatedObject);
            }
            return ret;
        } else {
            return null;
        }
    }
}
