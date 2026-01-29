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
    protected Map<UUID, FieldValue> fields = new LinkedHashMap<>(); // key: twinClassFieldId
    protected Set<UUID> markersAdd;
    protected Set<String> tagsAddNew;
    protected Set<UUID> tagsAddExisted;
    protected String commentAdd;
    //this flag helps to simply avoid recursion factory task call during creates/updates,
    //so currently we do not support cascade factory call during such operations
    private boolean canTriggerAfterOperationFactory = true;

    public TwinSave addField(FieldValue fieldValue) {
        if (fields == null)
            fields = new HashMap<>();
        fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        return this;
    }

    public TwinSave addFields(List<FieldValue> fieldValueList) {
        if (fields == null)
            fields = new HashMap<>();
        for (FieldValue fieldValue : fieldValueList)
            fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        return this;
    }

    public FieldValue getField(UUID twinClassFieldId) {
        return fields != null ? fields.get(twinClassFieldId) : null;
    }

    public TwinSave setFields(List<FieldValue> fieldValueList) {
        if (fieldValueList != null) {
            fields = new HashMap<>();
            for (FieldValue fieldValue : fieldValueList)
                fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        }
        return this;
    }

    public TwinSave addMarker(UUID marker) {
        if (markersAdd == null)
            markersAdd = new HashSet<>();
        markersAdd.add(marker);
        return this;
    }

    public abstract UUID nullifyUUID();
}

