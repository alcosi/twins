package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "fieldType")
@JsonSubTypes({
                @JsonSubTypes.Type(value = TwinFieldValueTextDTOv1.class, name = TwinFieldValueTextDTOv1.KEY),
                @JsonSubTypes.Type(value = TwinFieldValueDateDTOv1.class, name = TwinFieldValueDateDTOv1.KEY),
                @JsonSubTypes.Type(value = TwinFieldValueColorHexDTOv1.class, name = TwinFieldValueColorHexDTOv1.KEY),
                @JsonSubTypes.Type(value = TwinFieldValueDataListOptionsDTOv1.class, name = TwinFieldValueDataListOptionsDTOv1.KEY),
})
@Schema(description = "On of values", example = "", oneOf = {
        TwinFieldValueTextDTOv1.class,
        TwinFieldValueColorHexDTOv1.class,
        TwinFieldValueDateDTOv1.class,
        TwinFieldValueDataListOptionsDTOv1.class}, discriminatorProperty = "fieldType" , discriminatorMapping = {
        @DiscriminatorMapping(value = TwinFieldValueTextDTOv1.KEY, schema = TwinFieldValueTextDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldValueDateDTOv1.KEY, schema = TwinFieldValueDateDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldValueColorHexDTOv1.KEY, schema = TwinFieldValueColorHexDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldValueDataListOptionsDTOv1.KEY, schema = TwinFieldValueDataListOptionsDTOv1.class),
})
public interface TwinFieldValueDTO {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED, examples = {
            TwinFieldValueTextDTOv1.KEY,
            TwinFieldValueColorHexDTOv1.KEY,
            TwinFieldValueDateDTOv1.KEY,
            TwinFieldValueDataListOptionsDTOv1.KEY,
    })
    public String fieldType();
}
