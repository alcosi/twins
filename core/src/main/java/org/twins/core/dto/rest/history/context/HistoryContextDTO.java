package org.twins.core.dto.rest.history.context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDescriptorUserLongDTOv1;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "contextType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HistoryContextUserDTOv1.class, name = HistoryContextUserDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextUserMultiDTOv1.class, name = HistoryContextUserMultiDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextStatusDTOv1.class, name = HistoryContextStatusDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextTwinDTOv1.class, name = HistoryContextTwinDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextTwinMultiDTOv1.class, name = HistoryContextTwinMultiDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextAttachmentDTOv1.class, name = HistoryContextAttachmentDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextAttachmentUpdateDTOv1.class, name = HistoryContextAttachmentUpdateDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextLinkDTOv1.class, name = HistoryContextLinkDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextLinkUpdateDTOv1.class, name = HistoryContextLinkUpdateDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextListDTOv1.class, name = HistoryContextListDTOv1.KEY),
        @JsonSubTypes.Type(value = HistoryContextListMultiDTOv1.class, name = HistoryContextListMultiDTOv1.KEY),
        @JsonSubTypes.Type(value = TwinClassFieldDescriptorUserLongDTOv1.class, name = TwinClassFieldDescriptorUserLongDTOv1.KEY),
})
@Schema(description = "On of values", example = "", discriminatorProperty = "changeType", discriminatorMapping = {
        @DiscriminatorMapping(value = HistoryContextUserDTOv1.KEY, schema = HistoryContextUserDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextUserMultiDTOv1.KEY, schema = HistoryContextUserMultiDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextStatusDTOv1.KEY, schema = HistoryContextStatusDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextTwinDTOv1.KEY, schema = HistoryContextTwinDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextTwinMultiDTOv1.KEY, schema = HistoryContextTwinMultiDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextAttachmentDTOv1.KEY, schema = HistoryContextAttachmentDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextAttachmentUpdateDTOv1.KEY, schema = HistoryContextAttachmentUpdateDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextLinkDTOv1.KEY, schema = HistoryContextLinkDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextLinkUpdateDTOv1.KEY, schema = HistoryContextLinkUpdateDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextListDTOv1.KEY, schema = HistoryContextListDTOv1.class),
        @DiscriminatorMapping(value = HistoryContextListMultiDTOv1.KEY, schema = HistoryContextListMultiDTOv1.class),
        @DiscriminatorMapping(value = TwinClassFieldDescriptorUserLongDTOv1.KEY, schema = TwinClassFieldDescriptorUserLongDTOv1.class),
})
public interface HistoryContextDTO {
    @Schema(description = "discriminator", requiredMode = Schema.RequiredMode.REQUIRED, examples = {
            HistoryContextUserDTOv1.KEY,
            HistoryContextUserMultiDTOv1.KEY,
            HistoryContextStatusDTOv1.KEY,
            HistoryContextTwinDTOv1.KEY,
            HistoryContextTwinMultiDTOv1.KEY,
            HistoryContextAttachmentDTOv1.KEY,
            HistoryContextAttachmentUpdateDTOv1.KEY,
            HistoryContextLinkDTOv1.KEY,
            HistoryContextLinkUpdateDTOv1.KEY,
            HistoryContextListDTOv1.KEY,
            HistoryContextListMultiDTOv1.KEY,
            TwinClassFieldDescriptorUserLongDTOv1.KEY,
    })
    String contextType();
}
