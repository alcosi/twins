package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.domain.DataTimeRange;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class DomainBusinessAccountSearch {

    Set<UUID> businessAccountIdList;
    Set<UUID> businessAccountIdExcludeList;
    Set<String> businessAccountNameLikeList;
    Set<String> businessAccountNameNotLikeList;
    Set<UUID> permissionSchemaIdList;
    Set<UUID> permissionSchemaIdExcludeList;
    Set<UUID> twinflowSchemaIdList;
    Set<UUID> twinflowSchemaIdExcludeList;
    Set<UUID> twinClassSchemaIdList;
    Set<UUID> twinClassSchemaIdExcludeList;
    Set<UUID> tierIdList;
    Set<UUID> tierIdExcludeList;
    Set<UUID> notificationSchemaIdList;
    Set<UUID> notificationSchemaIdExcludeList;
    IntegerRange storageUsedSizeRange;
    IntegerRange storageUsedCountRange;
    DataTimeRange createAtRange;

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
