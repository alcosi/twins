package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldValueSelect extends FieldValue {
    private List<DataListOptionEntity> options = new ArrayList<>();

    public FieldValueSelect add(DataListOptionEntity option) {
        options.add(option);
        return this;
    }
}
