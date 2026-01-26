package org.twins.core.featurer.fieldtyper.descriptor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
public class FieldDescriptorList extends FieldDescriptor {
    private boolean supportCustom;
    private boolean multiple;
    private UUID dataListId;
    private UUID defaultDataListOptionId;
    private Set<UUID> dataListOptionIdList;
    private Set<UUID> dataListOptionIdExcludeList;
    private Set<UUID> dataListSubsetIdList;
    private Set<UUID> dataListSubsetIdExcludeList;
    private List<DataListOptionEntity> options; // be careful, null is a flag that this is a long list

    public FieldDescriptorList add(DataListOptionEntity option) {
        if (options == null) {
            options = new ArrayList<>();
        }
        options.add(option);
        return this;
    }

    public FieldDescriptorList applyUUIDSetIfNotEmpty(Set<UUID> source, java.util.function.Consumer<Set<UUID>> consumer) {
        if (source != null && !source.isEmpty())
            consumer.accept(source);
        return this;
    }
}
