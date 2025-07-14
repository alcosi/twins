package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        examples = {
                TwinFieldSearchTextDTOv1.KEY,
                TwinFieldSearchDateDTOv1.KEY,
                TwinFieldSearchNumericDTOv1.KEY,
                TwinFieldSearchListDTOv1.KEY,
                TwinFieldSearchIdDTOv1.KEY,
                TwinFieldSearchBooleanDTOv1.KEY,
                TwinFieldSearchUserDTOv1.KEY,
                TwinFieldSearchSpaceRoleUserDTOv1.KEY
        },
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        description = "Polymorphic filter by field value",
        discriminatorProperty = "type",
        discriminatorMapping = {
                @DiscriminatorMapping(value = TwinFieldSearchTextDTOv1.KEY, schema = TwinFieldSearchTextDTOv1.class),
                @DiscriminatorMapping(value = TwinFieldSearchDateDTOv1.KEY, schema = TwinFieldSearchDateDTOv1.class),
                @DiscriminatorMapping(value = TwinFieldSearchNumericDTOv1.KEY, schema = TwinFieldSearchNumericDTOv1.class),
                @DiscriminatorMapping(value = TwinFieldSearchListDTOv1.KEY, schema = TwinFieldSearchListDTOv1.class),
                @DiscriminatorMapping(value = TwinFieldSearchIdDTOv1.KEY, schema = TwinFieldSearchIdDTOv1.class),
                @DiscriminatorMapping(value = TwinFieldSearchBooleanDTOv1.KEY, schema = TwinFieldSearchBooleanDTOv1.class),
                @DiscriminatorMapping(value = TwinFieldSearchUserDTOv1.KEY, schema = TwinFieldSearchUserDTOv1.class),
                @DiscriminatorMapping(value = TwinFieldSearchSpaceRoleUserDTOv1.KEY, schema = TwinFieldSearchSpaceRoleUserDTOv1.class)
        },
        oneOf = {
                TwinFieldSearchTextDTOv1.class,
                TwinFieldSearchDateDTOv1.class,
                TwinFieldSearchNumericDTOv1.class,
                TwinFieldSearchListDTOv1.class,
                TwinFieldSearchIdDTOv1.class,
                TwinFieldSearchBooleanDTOv1.class,
                TwinFieldSearchUserDTOv1.class,
                TwinFieldSearchSpaceRoleUserDTOv1.class
        }
)
public interface TwinFieldSearchDTOv1 {
    @Schema(hidden = true)
    default String type() {
        return null;
    }
}
