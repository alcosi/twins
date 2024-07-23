package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinflowSearch {
    Set<UUID> twinClassIdList;
    Set<UUID> twinClassIdExcludeList;
    Set<String> nameI18nLikeList;
    Set<String> descriptionI18nLikeList;
    Set<UUID> initialStatusIdList;
    Set<UUID> initialStatusIdExcludeList;


    public TwinflowSearch addTwinClassId(Collection<UUID> twinClassIdSet) {
        twinClassIdList = CollectionUtils.safeAdd(twinClassIdList, twinClassIdSet);
        return this;
    }

    public TwinflowSearch addTwinClassIdExlude(Collection<UUID> twinClassIdExcludeSet) {
        twinClassIdExcludeList = CollectionUtils.safeAdd(twinClassIdExcludeList, twinClassIdExcludeSet);
        return this;
    }

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
