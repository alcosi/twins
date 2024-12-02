package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;


@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<String> keyLikeList;
    Set<String> keyNotLikeList;
    Set<String> nameI18nLikeList;
    Set<String> nameI18nNotLikeList;
    Set<String> descriptionI18nLikeList;
    Set<String> descriptionI18nNotLikeList;
    Set<Integer> fieldTyperIdList;
    Set<Integer> fieldTyperIdExcludeList;
    Set<UUID> viewPermissionIdList;
    Set<UUID> viewPermissionIdExcludeList;
    Set<UUID> editPermissionIdList;
    Set<UUID> editPermissionIdExcludeList;
    Ternary required;
}
