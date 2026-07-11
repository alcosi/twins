package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true) // TWINS-254 bug with hashcode generation during insert in factory items set
@Accessors(chain = true)
public abstract class TwinSave extends TwinOperation {
    protected LinkedHashMap<UUID, FieldValue> fields; // key: twinClassFieldId
    protected Set<UUID> markersAdd;
    protected Set<String> tagsAddNew;
    protected Set<UUID> tagsAddExisted;
    protected LinkedHashSet<String> commentsAdd;
    //this flag helps to simply avoid recursion factory task call during creates/updates,
    //so currently we do not support cascade factory call during such operations
    private boolean canTriggerAfterOperationFactory = true;
    // Remaining budget of the on-create/on-update factory cascade for this operation.
    // null = direct (top-level) operation, resolved to the configured twins.factory.cascade.max-depth.
    // A positive value = cascade extra that may still re-trigger its own on-factory; reaching 0 stops
    // the cascade. Managed by TwinflowFactoryService.runFactoryOn / cascadeApplyExtras and read by the
    // runFactoryOnCreate / runFactoryOnUpdate guards in TwinService.
    private Integer cascadeDepth;

    public TwinSave addField(FieldValue fieldValue) {
        if (fields == null)
            fields = new LinkedHashMap<>();
        fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        return this;
    }

    public TwinSave addFields(List<FieldValue> fieldValueList) {
        if (fields == null)
            fields = new LinkedHashMap<>();
        for (FieldValue fieldValue : fieldValueList)
            fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        return this;
    }

    public FieldValue getField(UUID twinClassFieldId) {
        return fields != null ? fields.get(twinClassFieldId) : null;
    }

    public TwinSave setFields(List<FieldValue> fieldValueList) {
        if (fieldValueList != null) {
            fields = new LinkedHashMap<>();
            for (FieldValue fieldValue : fieldValueList)
                fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        }
        return this;
    }

    public TwinSave setFields(LinkedHashMap<UUID, FieldValue> newFields) {
        fields = newFields;
        return this;
    }

    public TwinSave addMarker(UUID marker) {
        if (markersAdd == null)
            markersAdd = new HashSet<>();
        markersAdd.add(marker);
        return this;
    }

    public TwinSave addComment(String comment) {
        if (commentsAdd == null)
            commentsAdd = new LinkedHashSet<>();
        commentsAdd.add(comment);
        return this;
    }

    public abstract UUID nullifyUUID();
}

