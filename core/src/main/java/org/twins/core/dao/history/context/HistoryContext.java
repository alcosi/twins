package org.twins.core.dao.history.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.FieldSnapshot;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.history.HistoryMutableDataCollector;

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
                name = HistoryContextDatalistChange.DISCRIMINATOR,
                value = HistoryContextDatalistChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextDatalistMultiChange.DISCRIMINATOR,
                value = HistoryContextDatalistMultiChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextUserMultiChange.DISCRIMINATOR,
                value = HistoryContextUserMultiChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextLink.DISCRIMINATOR,
                value = HistoryContextLink.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextLinkChange.DISCRIMINATOR,
                value = HistoryContextLinkChange.class
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
@Data
@Accessors(chain = true)
public abstract class HistoryContext implements Serializable {
    public static final String PLACEHOLDER_FIELD = "field";

    @JsonIgnore
    private HashMap<String, String> templateVars;

    public void setType(String type) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public abstract String getType();

    protected FieldSnapshot field; //in case of changes of field


    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        FieldSnapshot.extractTemplateVars(vars, field, PLACEHOLDER_FIELD);
        String fromValue = templateFromValue();
        String toValue = templateToValue();
        vars.put("fromValue", fromValue != null ? fromValue : "");
        vars.put("toValue", toValue != null ? toValue : "");
        return vars;
    }

    public abstract String templateFromValue();

    public abstract String templateToValue();

    public HashMap<String, String> getTemplateVars() {
        if (templateVars == null)
            templateVars = extractTemplateVars();
        return templateVars;
    }

    public HistoryContext shotField(TwinClassFieldEntity fieldEntity, I18nService i18nService) {
        field = FieldSnapshot.convertEntity(fieldEntity, i18nService);
        return this;
    }

    public boolean collectMutableData(String messageTemplate, HistoryMutableDataCollector mutableDataCollector) {
        //no need to check field snapshot cause it is linked by FK in history table
        return false;
    }

    protected static boolean containPlaceHolder(String messageTemplate, String placeholderPrefix) {
        return messageTemplate.contains("${" + placeholderPrefix);
    }

    public void spoofSnapshots(HistoryMutableDataCollector mutableDataCollector) {

    }
}
