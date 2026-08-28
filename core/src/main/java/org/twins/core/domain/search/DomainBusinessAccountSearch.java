package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.SetUtils;
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
        return SetUtils.safeAddAll(this, ids, exclude,
                this::getBusinessAccountIdList, this::setBusinessAccountIdList,
                this::getBusinessAccountIdExcludeList, this::setBusinessAccountIdExcludeList);
    }

    public DomainBusinessAccountSearch addPermissionSchemaId(Collection<UUID> ids, boolean exclude) {
        return SetUtils.safeAddAll(this, ids, exclude,
                this::getPermissionSchemaIdList, this::setPermissionSchemaIdList,
                this::getPermissionSchemaIdExcludeList, this::setPermissionSchemaIdExcludeList);
    }

    public DomainBusinessAccountSearch addTwinflowSchemaId(Collection<UUID> ids, boolean exclude) {
        return SetUtils.safeAddAll(this, ids, exclude,
                this::getTwinflowSchemaIdList, this::setTwinflowSchemaIdList,
                this::getTwinflowSchemaIdExcludeList, this::setTwinflowSchemaIdExcludeList);
    }

    public DomainBusinessAccountSearch addTwinClassSchemaId(Collection<UUID> ids, boolean exclude) {
        return SetUtils.safeAddAll(this, ids, exclude,
                this::getTwinClassSchemaIdList, this::setTwinClassSchemaIdList,
                this::getTwinClassSchemaIdExcludeList, this::setTwinClassSchemaIdExcludeList);
    }

    public DomainBusinessAccountSearch addbusinessAccountNameLikeList(Collection<String> keyword, boolean exclude) {
        return SetUtils.safeAddAll(this, keyword, exclude,
                this::getBusinessAccountNameLikeList, this::setBusinessAccountNameLikeList,
                this::getBusinessAccountNameNotLikeList, this::setBusinessAccountNameNotLikeList);
    }

    public DomainBusinessAccountSearch addTierIdList(Collection<UUID> ids, boolean exclude){
        return SetUtils.safeAddAll(this, ids, exclude,
                this::getTierIdList, this::setTierIdList,
                this::getTierIdExcludeList, this::setTierIdExcludeList);
    }

    public DomainBusinessAccountSearch addNotificationSchemeIdList(Collection<UUID> ids, boolean exclude){
        return SetUtils.safeAddAll(this, ids, exclude,
                this::getNotificationSchemaIdList, this::setNotificationSchemaIdList,
                this::getNotificationSchemaIdExcludeList, this::setNotificationSchemaIdExcludeList);
    }

}
