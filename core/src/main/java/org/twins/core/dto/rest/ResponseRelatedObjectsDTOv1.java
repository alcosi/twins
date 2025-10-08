package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.ContainsRelatedObjects;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;

import java.util.Collection;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ResponseRelatedObjectsV1")
public class ResponseRelatedObjectsDTOv1 extends Response {

    public ResponseRelatedObjectsDTOv1() {
        super();
    }

    @Schema(description = "results - related objects, if lazeRelation is false")
    public RelatedObjectsDTOv1 relatedObjects;

    public <T extends ContainsRelatedObjects> void initWithRelatedObjects(Collection<T> objectsList) {
        if (objectsList == null) {
            return;
        }
        for (var obj : objectsList) {
            obj.setRelatedObjects(relatedObjects);
        }
    }

    public <T extends ContainsRelatedObjects> void initWithRelatedObjects(T object) {
        if (object == null) {
            return;
        }
        object.setRelatedObjects(relatedObjects);
    }
}
