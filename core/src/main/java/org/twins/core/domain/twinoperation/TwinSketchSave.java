package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinSketchSave {
    private Map<UUID, FieldValue> fields;

    public FieldValue getField(UUID twinClassFieldId) {
        return fields != null ? fields.get(twinClassFieldId) : null;
    }

    public TwinSketchSave setFields(List<FieldValue> fieldValueList) {
        if (fieldValueList != null) {
            fields = new HashMap<>();
            for (FieldValue fieldValue : fieldValueList)
                fields.put(fieldValue.getTwinClassField().getId(), fieldValue);
        }
        return this;
    }

}
