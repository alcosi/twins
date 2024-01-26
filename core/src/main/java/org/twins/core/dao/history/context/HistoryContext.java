package org.twins.core.dao.history.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.HashMap;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                name = HistoryContextAttachment.DISCRIMINATOR,
                value = HistoryContextAttachment.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextAttachmentChange.DISCRIMINATOR,
                value = HistoryContextAttachmentChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextFieldDatalistChange.DISCRIMINATOR,
                value = HistoryContextFieldDatalistChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextFieldDatalistMultiChange.DISCRIMINATOR,
                value = HistoryContextFieldDatalistMultiChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextFieldSimpleChange.DISCRIMINATOR,
                value = HistoryContextFieldSimpleChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextStatusChange.DISCRIMINATOR,
                value = HistoryContextStatusChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextStringChange.DISCRIMINATOR,
                value = HistoryContextStringChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextTwinChange.DISCRIMINATOR,
                value = HistoryContextTwinChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextUserChange.DISCRIMINATOR,
                value = HistoryContextUserChange.class
        ),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class HistoryContext implements Serializable {
    @JsonIgnore
    private HashMap<String, String> templateVars;
    public void setType(String type) {

    }
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public abstract String getType();

//    public abstract boolean equals(Object o);
//
//    public abstract int hashCode();

    public HashMap<String, String> getTemplateVars() {
        if (templateVars == null)
            templateVars = extractTemplateVars();
        return templateVars;
    }

    protected abstract HashMap<String, String> extractTemplateVars();
}
