package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextFieldUserMultiChange extends HistoryContextFieldChange {
    public static final String DISCRIMINATOR = "history.fieldChange.userMulti";
    private List<UUID> addedUserIdList;
    private List<UUID> deletedUserIdList;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        //todo loop lists and add more template vars
        return vars;
    }

    public HistoryContextFieldUserMultiChange shotAddedUserId(UUID userId) {
        addedUserIdList = CollectionUtils.safeAdd(addedUserIdList, userId);
        return this;
    }

    public HistoryContextFieldUserMultiChange shotDeletedUserId(UUID userId) {
        deletedUserIdList = CollectionUtils.safeAdd(deletedUserIdList, userId);
        return this;
    }

    public boolean notEmpty() {
        return org.apache.commons.collections4.CollectionUtils.isEmpty(addedUserIdList) || org.apache.commons.collections4.CollectionUtils.isEmpty(deletedUserIdList);
    }
}
