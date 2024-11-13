package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.user.UserStatus;

import java.util.*;

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
}
