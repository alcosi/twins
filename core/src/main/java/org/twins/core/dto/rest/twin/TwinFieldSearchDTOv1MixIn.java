package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TwinFieldSearchTextDTOv1.class, name = TwinFieldSearchTextDTOv1.KEY),
    @JsonSubTypes.Type(value = TwinFieldSearchDateDTOv1.class, name = TwinFieldSearchDateDTOv1.KEY),
    @JsonSubTypes.Type(value = TwinFieldSearchNumericDTOv1.class, name = TwinFieldSearchNumericDTOv1.KEY),
    @JsonSubTypes.Type(value = TwinFieldSearchListDTOv1.class, name = TwinFieldSearchListDTOv1.KEY),
    @JsonSubTypes.Type(value = TwinFieldSearchIdDTOv1.class, name = TwinFieldSearchIdDTOv1.KEY),
    @JsonSubTypes.Type(value = TwinFieldSearchBooleanDTOv1.class, name = TwinFieldSearchBooleanDTOv1.KEY),
    @JsonSubTypes.Type(value = TwinFieldSearchUserDTOv1.class, name = TwinFieldSearchUserDTOv1.KEY),
    @JsonSubTypes.Type(value = TwinFieldSearchSpaceRoleUserDTOv1.class, name = TwinFieldSearchSpaceRoleUserDTOv1.KEY)
})
public interface TwinFieldSearchDTOv1MixIn {
}
