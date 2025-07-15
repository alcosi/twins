package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "fieldType"
)
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
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorBooleanDTOv1.class, name = TwinClassFieldDescriptorBooleanDTOv1.KEY),
})
public interface TwinClassFieldDescriptorDTOMixIn {
}
