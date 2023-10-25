package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ResponseRelatedObjectsV1")
public class ResponseRelatedObjectsDTOv1 extends Response {
    @Schema(description = "results - related objects, if lazeRelation is false")
    public RelatedObjectsDTOv1 relatedObjects;
}
