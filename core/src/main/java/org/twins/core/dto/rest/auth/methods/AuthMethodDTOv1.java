package org.twins.core.dto.rest.auth.methods;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthMethodPasswordDTOv1.class, name = AuthMethodPasswordDTOv1.KEY),
        @JsonSubTypes.Type(value = AuthMethodOath2DTOv1.class, name = AuthMethodOath2DTOv1.KEY),
        @JsonSubTypes.Type(value = AuthMethodStubDTOv1.class, name = AuthMethodStubDTOv1.KEY)
})
@Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE, description = "One of values", discriminatorProperty = "type", discriminatorMapping = {
        @DiscriminatorMapping(value = AuthMethodPasswordDTOv1.KEY, schema = AuthMethodPasswordDTOv1.class),
        @DiscriminatorMapping(value = AuthMethodOath2DTOv1.KEY, schema = AuthMethodOath2DTOv1.class),
        @DiscriminatorMapping(value = AuthMethodStubDTOv1.KEY, schema = AuthMethodStubDTOv1.class)
})
public interface AuthMethodDTOv1 {
    public String type();
}
