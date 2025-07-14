package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

// On adding the new implementation, remember about HistoryContextDTOMixIn.class & ApplicationConfig.class
@Schema(
        examples = {
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
                HistoryContextListMultiDTOv1.KEY
        },
        additionalProperties = Schema.AdditionalPropertiesValue.FALSE,
        description = "Polymorphic history context",
        discriminatorProperty = "contextType",
        discriminatorMapping = {
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
        },
        oneOf = {
                HistoryContextUserDTOv1.class,
                HistoryContextUserMultiDTOv1.class,
                HistoryContextStatusDTOv1.class,
                HistoryContextTwinDTOv1.class,
                HistoryContextTwinMultiDTOv1.class,
                HistoryContextAttachmentDTOv1.class,
                HistoryContextAttachmentUpdateDTOv1.class,
                HistoryContextLinkDTOv1.class,
                HistoryContextLinkUpdateDTOv1.class,
                HistoryContextListDTOv1.class,
                HistoryContextListMultiDTOv1.class
        }
)
public interface HistoryContextDTO {
    @Schema(hidden = true)
    default String contextType() { return null; }
}
