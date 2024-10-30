package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

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


}
