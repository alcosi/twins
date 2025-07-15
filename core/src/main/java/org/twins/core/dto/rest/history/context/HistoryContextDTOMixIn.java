package org.twins.core.dto.rest.history.context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "contextType"
)
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
        @JsonSubTypes.Type(value = HistoryContextListMultiDTOv1.class, name = HistoryContextListMultiDTOv1.KEY)
})
public interface HistoryContextDTOMixIn {
}
