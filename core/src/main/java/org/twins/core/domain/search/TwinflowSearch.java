package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinflowSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Map<UUID, Boolean> twinClassIdMap;
    private Map<UUID, Boolean> twinClassIdExcludeMap;
    private Set<String> nameI18nLikeList;
    private Set<String> nameI18nNotLikeList;
    private Set<String> descriptionI18nLikeList;
    private Set<String> descriptionI18nNotLikeList;
    private Set<UUID> initialStatusIdList;
    private Set<UUID> initialStatusIdExcludeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
    private Set<UUID> twinflowSchemaIdList;
    private Set<UUID> twinflowSchemaIdExcludeList;

    public TwinflowSearch addNameLike(Collection<String> nameLikeSet) {
        nameI18nLikeList = CollectionUtils.safeAdd(nameI18nLikeList, nameLikeSet);
        return this;
    }

    public TwinflowSearch addDescriptionLike(Collection<String> descriptionLikeSet) {
        descriptionI18nLikeList = CollectionUtils.safeAdd(descriptionI18nLikeList, descriptionLikeSet);
        return this;
    }

    public TwinflowSearch addInitialStatusId(Collection<UUID> initialStatusIdSet) {
        initialStatusIdList = CollectionUtils.safeAdd(initialStatusIdList, initialStatusIdSet);
        return this;
    }

    public TwinflowSearch addInitialStatusIdExclude(Collection<UUID> initialStatusIdExcludeSet) {
        initialStatusIdExcludeList = CollectionUtils.safeAdd(initialStatusIdExcludeList, initialStatusIdExcludeSet);
        return this;
    }

}
