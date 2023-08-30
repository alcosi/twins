package org.twins.core.featurer.fieldtyper;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Hashtable;

@Data
@Accessors(fluent = true)
public class FieldTypeUIDescriptor {
    private String type;
    private Hashtable<String, Object> params = new Hashtable<>();

    public FieldTypeUIDescriptor addParam(String key, Object value) {
        params.put(key, value);
        return this;
    }
}
