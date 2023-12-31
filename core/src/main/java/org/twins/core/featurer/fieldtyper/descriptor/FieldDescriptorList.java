package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorList extends FieldDescriptor {
    private boolean supportCustom;
    private boolean multiple;
    private UUID dataListId;
    private List<DataListOptionEntity> options = new ArrayList<>();

    public FieldDescriptorList add(DataListOptionEntity option) {
        options.add(option);
        return this;
    }
}
