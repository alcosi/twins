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
public class HistoryContextUserMultiChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.userMultiChange";
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

    @Override
    public String templateFromValue() {
        return null; //todo any idea?
    }

    @Override
    public String templateToValue() {
        return null; //todo any idea?
    }

    public HistoryContextUserMultiChange shotAddedUserId(UUID userId) {
        addedUserIdList = CollectionUtils.safeAdd(addedUserIdList, userId);
        return this;
    }

    public HistoryContextUserMultiChange shotDeletedUserId(UUID userId) {
        deletedUserIdList = CollectionUtils.safeAdd(deletedUserIdList, userId);
        return this;
    }

    public boolean notEmpty() {
        return org.apache.commons.collections4.CollectionUtils.isEmpty(addedUserIdList) || org.apache.commons.collections4.CollectionUtils.isEmpty(deletedUserIdList);
    }
}
