package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

//USED! in jackson serialization(reverse mappers)
//be sure that polymorph classes do not has the same-named fields.
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TwinFieldSearchTextDTOv1.class, name = TwinFieldSearchTextDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchDateDTOv1.class, name = TwinFieldSearchDateDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchNumericDTOv1.class, name = TwinFieldSearchNumericDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinFieldSearchListDTOv1.class, name = TwinFieldSearchListDTOv1.KEY),
})
@Schema(description = "One of values", discriminatorProperty = "type", discriminatorMapping = {
        @DiscriminatorMapping(value = TwinFieldSearchTextDTOv1.KEY, schema = TwinFieldSearchTextDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldSearchDateDTOv1.KEY, schema = TwinFieldSearchDateDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldSearchNumericDTOv1.KEY, schema = TwinFieldSearchNumericDTOv1.class),
        @DiscriminatorMapping(value = TwinFieldSearchListDTOv1.KEY, schema = TwinFieldSearchListDTOv1.class),
})
public interface TwinFieldSearchDTOv1 {

    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED, examples = {
            TwinFieldSearchTextDTOv1.KEY,
            TwinFieldSearchNumericDTOv1.KEY,
            TwinFieldSearchDateDTOv1.KEY,
            TwinFieldSearchListDTOv1.KEY,
    })
    String type();
}
