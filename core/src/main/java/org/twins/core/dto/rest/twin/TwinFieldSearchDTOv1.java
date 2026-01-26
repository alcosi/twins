package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TwinFieldSearchTextDTOv1.class, name = TwinFieldSearchTextDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchDateDTOv1.class, name = TwinFieldSearchDateDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchNumericDTOv1.class, name = TwinFieldSearchNumericDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchListDTOv1.class, name = TwinFieldSearchListDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchIdDTOv1.class, name = TwinFieldSearchIdDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchBooleanDTOv1.class, name = TwinFieldSearchBooleanDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchUserDTOv1.class, name = TwinFieldSearchUserDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchSpaceRoleUserDTOv1.class, name = TwinFieldSearchSpaceRoleUserDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchTwinClassListDTOv1.class, name = TwinFieldSearchTwinClassListDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchTimestampDTOv1.class, name = TwinFieldSearchTimestampDTOv1.KEY)
})
@Schema(
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        description = "One of values",
        discriminatorProperty = "type",
        oneOf = {
                TwinFieldSearchTextDTOv1.class,
                TwinFieldSearchDateDTOv1.class,
                TwinFieldSearchNumericDTOv1.class,
                TwinFieldSearchListDTOv1.class,
                TwinFieldSearchIdDTOv1.class,
                TwinFieldSearchBooleanDTOv1.class,
                TwinFieldSearchUserDTOv1.class,
                TwinFieldSearchSpaceRoleUserDTOv1.class,
                TwinFieldSearchTwinClassListDTOv1.class,
                TwinFieldSearchTimestampDTOv1.class
        }
)
public interface TwinFieldSearchDTOv1 {
    @Schema(description = "Discriminator for search type", requiredMode = Schema.RequiredMode.REQUIRED)
    String type();
}
