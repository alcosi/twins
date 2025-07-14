package org.twins.core.dto.rest.transition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "fieldType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TwinTransitionPerformResultMinorDTOv1.class, name = TwinTransitionPerformResultMinorDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinTransitionPerformResultMajorDTOv1.class, name = TwinTransitionPerformResultMajorDTOv1.KEY)
})
public interface TwinTransitionPerformResultDTOMixIn {
}
