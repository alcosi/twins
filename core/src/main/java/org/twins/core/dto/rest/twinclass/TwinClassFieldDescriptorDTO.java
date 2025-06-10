package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "fieldType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorTextDTOv1.class, name = TwinClassFieldDescriptorTextDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorSecretDTOv1.class, name = TwinClassFieldDescriptorSecretDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorDateScrollDTOv1.class, name = TwinClassFieldDescriptorDateScrollDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorColorHexDTOv1.class, name = TwinClassFieldDescriptorColorHexDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorUrlDTOv1.class, name = TwinClassFieldDescriptorUrlDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorListDTOv1.class, name = TwinClassFieldDescriptorListDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorListLongDTOv1.class, name = TwinClassFieldDescriptorListLongDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorListSharedInHeadDTOv1.class, name = TwinClassFieldDescriptorListSharedInHeadDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorLinkDTOv1.class, name = TwinClassFieldDescriptorLinkDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorLinkLongDTOv1.class, name = TwinClassFieldDescriptorLinkLongDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorI18nDTOv1.class, name = TwinClassFieldDescriptorI18nDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorUserDTOv1.class, name = TwinClassFieldDescriptorUserDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorUserLongDTOv1.class, name = TwinClassFieldDescriptorUserLongDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorAttachmentDTOv1.class, name = TwinClassFieldDescriptorAttachmentDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorNumericDTOv1.class, name = TwinClassFieldDescriptorNumericDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorImmutableDTOv1.class, name = TwinClassFieldDescriptorImmutableDTOv1.KEY),
})
@Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE, description = "One of values", example = "", discriminatorProperty = "fieldType", discriminatorMapping = {
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
})
public interface TwinClassFieldDescriptorDTO {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED, examples = {
            TwinClassFieldDescriptorTextDTOv1.KEY,
            TwinClassFieldDescriptorSecretDTOv1.KEY,
            TwinClassFieldDescriptorColorHexDTOv1.KEY,
            TwinClassFieldDescriptorUrlDTOv1.KEY,
            TwinClassFieldDescriptorDateScrollDTOv1.KEY,
            TwinClassFieldDescriptorListDTOv1.KEY,
            TwinClassFieldDescriptorListLongDTOv1.KEY,
            TwinClassFieldDescriptorLinkDTOv1.KEY,
            TwinClassFieldDescriptorLinkLongDTOv1.KEY,
            TwinClassFieldDescriptorI18nDTOv1.KEY,
            TwinClassFieldDescriptorListSharedInHeadDTOv1.KEY,
            TwinClassFieldDescriptorUserDTOv1.KEY,
            TwinClassFieldDescriptorUserLongDTOv1.KEY,
            TwinClassFieldDescriptorAttachmentDTOv1.KEY,
            TwinClassFieldDescriptorNumericDTOv1.KEY,
            TwinClassFieldDescriptorImmutableDTOv1.KEY,
    })
    public String fieldType();
}
