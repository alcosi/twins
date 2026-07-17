package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.domain.search.EntitySearch;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinPointerSearch extends EntitySearch<TwinPointerEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinClassIdList;
    private Set<UUID> twinClassIdExcludeList;
    private Set<Integer> pointerFeaturerIdList;
    private Set<Integer> pointerFeaturerIdExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
}
