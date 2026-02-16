package org.twins.core.dao.history.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.HistoryContextDecimalChange;
import org.twins.core.dao.history.context.snapshot.FieldSnapshot;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.service.i18n.I18nService;

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
        @JsonSubTypes.Type(
                name = HistoryContextComment.DISCRIMINATOR,
                value = HistoryContextComment.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextSpaceRoleUserChange.DISCRIMINATOR,
                value = HistoryContextSpaceRoleUserChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextTimestampChange.DISCRIMINATOR,
                value = HistoryContextTimestampChange.class
        ),
        @JsonSubTypes.Type(
                name = HistoryContextDecimalChange.DISCRIMINATOR,
                value = HistoryContextDecimalChange.class
        )
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public abstract class HistoryContext implements Serializable {
    @JsonIgnore
    private HashMap<String, String> templateVars;
    public void setType(String type) {}
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public abstract String getType();

    protected FieldSnapshot field; //in case of changes of field


    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        FieldSnapshot.extractTemplateVars(vars, field, "field");
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
}
