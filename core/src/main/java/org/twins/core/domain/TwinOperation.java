package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;

@Data
@Accessors(chain = true)
public abstract class TwinOperation {
    protected TwinEntity twinEntity; // only for new/updated data
    protected Map<UUID, FieldValue> fields; // key: twinClassFieldId
    protected Set<UUID> markersAdd;


    public TwinOperation addField(FieldValue fieldValue) {
        if (fields == null)
            fields = new HashMap<>();
        fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        return this;
    }

    public TwinOperation addFields(List<FieldValue> fieldValueList) {
        if (fields == null)
            fields = new HashMap<>();
        for (FieldValue fieldValue : fieldValueList)
            fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        return this;
    }

    public TwinOperation setFields(List<FieldValue> fieldValueList) {
        if (fieldValueList != null) {
            fields = new HashMap<>();
            for (FieldValue fieldValue : fieldValueList)
                fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        }
        return this;
    }

    public TwinOperation addMarker(UUID marker) {
        if (markersAdd == null)
            markersAdd = new HashSet<>();
        markersAdd.add(marker);
        return this;
    }



    public abstract UUID nullifyUUID();
}
