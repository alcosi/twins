package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

// On adding the new implementation, remember about TwinClassFieldDescriptorDTOMixIn.class & JacksonConfig.class
@Schema(
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        description = "Polymorphic twin class field descriptor",
        discriminatorProperty = "fieldType",
        discriminatorMapping = {
                @DiscriminatorMapping(value = TwinClassFieldDescriptorTextDTOv1.KEY, schema = TwinClassFieldDescriptorTextDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorSecretDTOv1.KEY, schema = TwinClassFieldDescriptorSecretDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorDateScrollDTOv1.KEY, schema = TwinClassFieldDescriptorDateScrollDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorColorHexDTOv1.KEY, schema = TwinClassFieldDescriptorColorHexDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorUrlDTOv1.KEY, schema = TwinClassFieldDescriptorUrlDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorListDTOv1.KEY, schema = TwinClassFieldDescriptorListDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorListLongDTOv1.KEY, schema = TwinClassFieldDescriptorListLongDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorListSharedInHeadDTOv1.KEY, schema = TwinClassFieldDescriptorListSharedInHeadDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorLinkDTOv1.KEY, schema = TwinClassFieldDescriptorLinkDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorLinkLongDTOv1.KEY, schema = TwinClassFieldDescriptorLinkLongDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorI18nDTOv1.KEY, schema = TwinClassFieldDescriptorI18nDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorUserDTOv1.KEY, schema = TwinClassFieldDescriptorUserDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorUserLongDTOv1.KEY, schema = TwinClassFieldDescriptorUserLongDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorAttachmentDTOv1.KEY, schema = TwinClassFieldDescriptorAttachmentDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorNumericDTOv1.KEY, schema = TwinClassFieldDescriptorNumericDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorImmutableDTOv1.KEY, schema = TwinClassFieldDescriptorImmutableDTOv1.class),
                @DiscriminatorMapping(value = TwinClassFieldDescriptorBooleanDTOv1.KEY, schema = TwinClassFieldDescriptorBooleanDTOv1.class)
        },
        oneOf = {
                TwinClassFieldDescriptorTextDTOv1.class,
                TwinClassFieldDescriptorSecretDTOv1.class,
                TwinClassFieldDescriptorDateScrollDTOv1.class,
                TwinClassFieldDescriptorColorHexDTOv1.class,
                TwinClassFieldDescriptorUrlDTOv1.class,
                TwinClassFieldDescriptorListDTOv1.class,
                TwinClassFieldDescriptorListLongDTOv1.class,
                TwinClassFieldDescriptorListSharedInHeadDTOv1.class,
                TwinClassFieldDescriptorLinkDTOv1.class,
                TwinClassFieldDescriptorLinkLongDTOv1.class,
                TwinClassFieldDescriptorI18nDTOv1.class,
                TwinClassFieldDescriptorUserDTOv1.class,
                TwinClassFieldDescriptorUserLongDTOv1.class,
                TwinClassFieldDescriptorAttachmentDTOv1.class,
                TwinClassFieldDescriptorNumericDTOv1.class,
                TwinClassFieldDescriptorImmutableDTOv1.class,
                TwinClassFieldDescriptorBooleanDTOv1.class
        }
)
public interface TwinClassFieldDescriptorDTO {
    @Schema(hidden = true)
    default String fieldType() { return null; }
}
