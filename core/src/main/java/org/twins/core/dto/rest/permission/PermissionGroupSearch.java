package org.twins.core.dto.rest.permission;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class PermissionGroupSearch {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<UUID> twinClassIdList;
    public Set<UUID> twinClassIdExcludeList;
    public Set<String> keyLikeList;
    public Set<String> keyNotLikeList;
    public Set<String> nameLikeList;
    public Set<String> nameNotLikeList;
    public Set<String> descriptionLikeList;
    public Set<String> descriptionNotLikeList;
}
