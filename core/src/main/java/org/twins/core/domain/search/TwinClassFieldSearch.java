package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Map<UUID, Boolean> twinClassIdMap;
    private Map<UUID, Boolean> twinClassIdExcludeMap;
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
    private Set<String> nameI18nLikeList;
    private Set<String> nameI18nNotLikeList;
    private Set<String> descriptionI18nLikeList;
    private Set<String> descriptionI18nNotLikeList;
    private Set<String> externalIdLikeList;
    private Set<String> externalIdNotLikeList;
    private Set<Integer> fieldTyperIdList;
    private Set<Integer> fieldTyperIdExcludeList;
    private Set<UUID> viewPermissionIdList;
    private Set<UUID> viewPermissionIdExcludeList;
    private Set<UUID> editPermissionIdList;
    private Set<UUID> editPermissionIdExcludeList;
    private Ternary required;

    public TwinClassFieldSearch addTwinClassId(final UUID id, final boolean searchExtends, boolean exclude) {
        if (exclude) {
            if (twinClassIdExcludeMap == null) {
                twinClassIdExcludeMap = new HashMap<>();
            }
            twinClassIdExcludeMap.put(id, searchExtends);
        } else {
            if (twinClassIdMap == null) {
                twinClassIdMap = new HashMap<>();
            }
            twinClassIdMap.put(id, searchExtends);
        }
        return this;
    }

    public TwinClassFieldSearch addId(final UUID id, boolean exclude) {
        if (exclude)
            idExcludeList = CollectionUtils.safeAdd(idExcludeList, id);
        else
            idList = CollectionUtils.safeAdd(idList, id);
        return this;
    }
}
