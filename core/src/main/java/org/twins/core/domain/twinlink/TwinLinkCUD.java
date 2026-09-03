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
 * the create list carries {@link TwinLinkCreate} composition objects (entity + creation-only relation
 * twin fields), while update/delete operate on plain entities.
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

    /** Convenience for factory fillers and other producers of plain entities. */
    public TwinLinkCUD addCreate(TwinLinkEntity twinLinkEntity) {
        TwinLinkCreate linkCreate = new TwinLinkCreate();
        linkCreate.setTwinLink(twinLinkEntity);
        getCreateListSafe().add(linkCreate);
        return this;
    }

    /** Entity view over the create list for consumers working with plain entities. */
    public List<TwinLinkEntity> getCreateEntityList() {
        return createList == null ? null : createList.stream().map(TwinLinkCreate::getTwinLink).collect(Collectors.toList());
    }

    /** Entity view over the update list for consumers working with plain entities. */
    public List<TwinLinkEntity> getUpdateEntityList() {
        return updateList == null ? null : updateList.stream().map(TwinLinkUpdate::getTwinLink).collect(Collectors.toList());
    }
}
