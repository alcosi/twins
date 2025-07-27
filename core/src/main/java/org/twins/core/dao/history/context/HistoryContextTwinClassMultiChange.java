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
public class HistoryContextTwinClassMultiChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.twinClassMultiChange";
    private List<UUID> addedTwinClassIdList;
    private List<UUID> deletedTwinClassIdList;

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

    public HistoryContextTwinClassMultiChange shotAddedTwinClassId(UUID twinClassId) {
        addedTwinClassIdList = CollectionUtils.safeAdd(addedTwinClassIdList, twinClassId);
        return this;
    }

    public HistoryContextTwinClassMultiChange shotDeletedTwinClassId(UUID twinClassId) {
        deletedTwinClassIdList = CollectionUtils.safeAdd(deletedTwinClassIdList, twinClassId);
        return this;
    }

    public boolean notEmpty() {
        return org.apache.commons.collections4.CollectionUtils.isEmpty(addedTwinClassIdList) || org.apache.commons.collections4.CollectionUtils.isEmpty(deletedTwinClassIdList);
    }
}
