package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "valueType")
@JsonSubTypes({
                @JsonSubTypes.Type(value = TwinFieldValueTextDTOv1.class, name = TwinFieldValueTextDTOv1.KEY),
                @JsonSubTypes.Type(value = TwinFieldValueDateDTOv1.class, name = TwinFieldValueDateDTOv1.KEY),
                @JsonSubTypes.Type(value = TwinFieldValueColorHexDTOv1.class, name = TwinFieldValueColorHexDTOv1.KEY),
                @JsonSubTypes.Type(value = TwinFieldValueListDTOv1.class, name = TwinFieldValueListDTOv1.KEY),
})
@Schema(description = "On of values", example = "", oneOf = {
        TwinFieldValueTextDTOv1.class,
        TwinFieldValueColorHexDTOv1.class,
        TwinFieldValueDateDTOv1.class,
        TwinFieldValueListDTOv1.class}, discriminatorProperty = "valueType" , discriminatorMapping = {
        @DiscriminatorMapping(value = TwinFieldValueTextDTOv1.KEY, schema = TwinFieldValueTextDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldValueDateDTOv1.KEY, schema = TwinFieldValueDateDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldValueColorHexDTOv1.KEY, schema = TwinFieldValueColorHexDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldValueListDTOv1.KEY, schema = TwinFieldValueListDTOv1.class),
})
public abstract class TwinFieldValueDTO {
    @JsonIgnore
    public UUID twinClassId;

    @JsonIgnore
    public String fieldKey;

    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED, examples = {
            TwinFieldValueTextDTOv1.KEY,
            TwinFieldValueColorHexDTOv1.KEY,
            TwinFieldValueDateDTOv1.KEY,
            TwinFieldValueListDTOv1.KEY,
    })
    public abstract String valueType();
}
