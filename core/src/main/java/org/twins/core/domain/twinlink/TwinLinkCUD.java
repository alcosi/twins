package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CUD - create, update, delete. Specialized replacement for the generic EntityCUD&lt;TwinLinkEntity&gt;:
 * the create list carries {@link TwinLinkCreate} declarative intents (twin + link + direction + far twins —
 * the rows are built by prepareTwinLinks), while update/delete operate on plain entities.
 */
@Data
@Accessors(chain = true)
public class TwinLinkCUD {
    private List<TwinLinkCreate> createList;
    private List<TwinLinkUpdate> updateList;
    private List<TwinLinkEntity> deleteList;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(createList) && CollectionUtils.isEmpty(updateList) && CollectionUtils.isEmpty(deleteList);
    }

    public List<TwinLinkCreate> getCreateListSafe() {
        if (createList == null)
            createList = new ArrayList<>();
        return createList;
    }

    public List<TwinLinkUpdate> getUpdateListSafe() {
        if (updateList == null)
            updateList = new ArrayList<>();
        return updateList;
    }

    public List<TwinLinkEntity> getDeleteListSafe() {
        if (deleteList == null)
            deleteList = new ArrayList<>();
        return deleteList;
    }

    public TwinLinkCUD addCreate(TwinLinkCreate linkCreate) {
        getCreateListSafe().add(linkCreate);
        return this;
    }

    /** Flattened built rows over the create list — meaningful after prepareTwinLinks ran. */
    public List<TwinLinkEntity> getCreateEntityList() {
        return createList == null ? null : createList.stream()
                .flatMap(linkCreate -> linkCreate.getTwinLinksSafe().stream())
                .collect(Collectors.toList());
    }

    /** Entity view over the update list for consumers working with plain entities. */
    public List<TwinLinkEntity> getUpdateEntityList() {
        return updateList == null ? null : updateList.stream().map(TwinLinkUpdate::getTwinLink).collect(Collectors.toList());
    }
}
