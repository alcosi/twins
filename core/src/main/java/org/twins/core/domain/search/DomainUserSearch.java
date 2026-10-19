package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.SetUtils;
import org.twins.core.enums.user.UserStatus;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DomainUserSearch {
    public Set<UUID> userIdList;
    public Set<UUID> userIdExcludeList;
    public Set<String> nameLikeList;
    public Set<String> nameNotLikeList;
    public Set<String> emailLikeList;
    public Set<String> emailNotLikeList;
    public Set<UserStatus> statusIdList;
    public Set<UserStatus> statusIdExcludeList;
    public Set<UUID> businessAccountIdList;
    public Set<UUID> businessAccountIdExcludeList;

    public DomainUserSearch addBusinessAccountId(UUID businessAccountId, boolean exclude) {
        return SetUtils.safeAdd(this, businessAccountId, exclude,
                this::getBusinessAccountIdList, this::setBusinessAccountIdList,
                this::getBusinessAccountIdExcludeList, this::setBusinessAccountIdExcludeList);
    }



}
