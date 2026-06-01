package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class DomainBusinessAccountSearch extends EntitySearch<DomainBusinessAccountEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<String> businessAccountNameLikeList;
    private Set<String> businessAccountNameNotLikeList;
    private Set<UUID> permissionSchemaIdList;
    private Set<UUID> permissionSchemaIdExcludeList;
    private Set<UUID> twinflowSchemaIdList;
    private Set<UUID> twinflowSchemaIdExcludeList;
    private Set<UUID> twinClassSchemaIdList;
    private Set<UUID> twinClassSchemaIdExcludeList;
    private Set<UUID> tierIdList;
    private Set<UUID> tierIdExcludeList;
    private Set<UUID> notificationSchemaIdList;
    private Set<UUID> notificationSchemaIdExcludeList;
    private IntegerRange storageUsedSizeRange;
    private IntegerRange storageUsedCountRange;
    private DataTimeRange createAtRange;


    public DomainBusinessAccountSearch addBusinessAccountId(Collection<UUID> ids, boolean exclude) {
        if (!exclude) businessAccountIdList = CollectionUtils.safeAdd(businessAccountIdList, ids);
        else businessAccountIdExcludeList = CollectionUtils.safeAdd(businessAccountIdExcludeList, ids);
        return this;
    }

    public DomainBusinessAccountSearch addPermissionSchemaId(Collection<UUID> ids, boolean exclude) {
        if (!exclude) permissionSchemaIdList = CollectionUtils.safeAdd(permissionSchemaIdList, ids);
        else permissionSchemaIdExcludeList = CollectionUtils.safeAdd(permissionSchemaIdExcludeList, ids);
        return this;
    }

    public DomainBusinessAccountSearch addTwinflowSchemaId(Collection<UUID> ids, boolean exclude) {
        if (!exclude) twinflowSchemaIdList = CollectionUtils.safeAdd(twinflowSchemaIdList, ids);
        else twinflowSchemaIdExcludeList = CollectionUtils.safeAdd(twinflowSchemaIdExcludeList, ids);
        return this;
    }

    public DomainBusinessAccountSearch addTwinClassSchemaId(Collection<UUID> ids, boolean exclude) {
        if (!exclude) twinClassSchemaIdList = CollectionUtils.safeAdd(twinClassSchemaIdList, ids);
        else twinClassSchemaIdExcludeList = CollectionUtils.safeAdd(twinClassSchemaIdExcludeList, ids);
        return this;
    }

    public DomainBusinessAccountSearch addbusinessAccountNameLikeList(Collection<String> keyword, boolean exclude) {
        if (!exclude) businessAccountNameLikeList = CollectionUtils.safeAdd(businessAccountNameLikeList, keyword);
        else businessAccountNameNotLikeList = CollectionUtils.safeAdd(businessAccountNameNotLikeList, keyword);
        return this;
    }

    public DomainBusinessAccountSearch addTierIdList(Collection<UUID> ids, boolean exclude){
        if (!exclude) tierIdList = CollectionUtils.safeAdd(tierIdList, ids);
        else tierIdExcludeList = CollectionUtils.safeAdd(tierIdExcludeList, ids);
        return this;
    }

    public DomainBusinessAccountSearch addNotificationSchemeIdList(Collection<UUID> ids, boolean exclude){
        if (!exclude) notificationSchemaIdList = CollectionUtils.safeAdd(notificationSchemaIdList, ids);
        else notificationSchemaIdExcludeList = CollectionUtils.safeAdd(notificationSchemaIdExcludeList, ids);
        return this;
    }

}
