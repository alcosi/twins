package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.StringUtils;
import org.twins.core.enums.factory.FactoryEraserAction;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinSave;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
public class FactoryItem implements EasyLoggable {
    // helps to identify on which branch current item was created.
    private FactoryBranchId factoryBranchId;
    @EqualsAndHashCode.Exclude
    private FactoryContext factoryContext;
    private TwinSave output;
    @EqualsAndHashCode.Exclude
    private List<FactoryItem> contextFactoryItemList;
    private EraseAction eraseAction = new EraseAction(FactoryEraserAction.NOT_SPECIFIED, "");
    // this will help to detect items, which were passed to factory from launch
    // all other items (created by multipliers) will have this flag set to false)
    private boolean factoryInputItem = false;

    public TwinEntity getTwin() {
        if (output == null)
            return null;
        if (output instanceof TwinUpdate twinUpdate)
            return twinUpdate.getDbTwinEntity();
        else
            return output.getTwinEntity();
    }

    public List<FactoryItem> getContextFactoryItemList() {
        if (contextFactoryItemList == null)
            contextFactoryItemList = new ArrayList<>();  //to be sure that no one set in to null
        return contextFactoryItemList;
    }

    public TwinEntity checkNotMultiplyContextTwin() throws ServiceException {
        FactoryItem contextItem = checkNotMultiplyContextItem();
        if (contextItem == null)
            return null;
        return contextItem.getTwin();
    }

    public FactoryItem checkNotMultiplyContextItem() throws ServiceException {
        if (getContextFactoryItemList() == null || getContextFactoryItemList().isEmpty())
            return null;
        else if (getContextFactoryItemList().size() > 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "context item size > 1. Please check multiplier");
        else
            return getContextFactoryItemList().get(0);
    }

    public TwinEntity checkSingleContextTwin() throws ServiceException {
        return checkSingleContextItem().getTwin();
    }

    public FactoryItem checkSingleContextItem() throws ServiceException {
        if (getContextFactoryItemList().size() != 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "context item size != 1. Please check multiplier");
        else
            return getContextFactoryItemList().get(0);
    }

    @Override
    public String easyLog(Level level) {
        String details = "";
        if (output == null || level == Level.SHORT)
            return "factoryItem[" + System.identityHashCode(this) + "]";
        String operation = output instanceof TwinCreate ? "createTwin" : "updateTwin";
        return switch (level) {
            case NORMAL -> "factoryItem[" + System.identityHashCode(this) + "] " + operation;
            default -> toString(1, 5, new HashSet<>());
        };
    }


    @Override
    public String toString() {
        return toString(1, 1, new HashSet<>());
    }

    public String toString(int currentDepth, int limit, Set<Integer> visited) {
        int hashCode = System.identityHashCode(this);
        if (currentDepth > limit || visited.contains(hashCode))
            return "";
        visited.add(hashCode);
        StringBuilder ret = new StringBuilder("factoryItem[" + hashCode + "]: " + (output instanceof TwinCreate ? "createTwin" : "updateTwin"));
        if (output.getTwinEntity() != null) {
            ret.append("[class:").append(output.getTwinEntity().getTwinClassId());
            ret.append(output instanceof TwinUpdate ? ", id:" + output.getTwinEntity().getId() : "");
            ret.append("]");
        }
        ret.append(" context[");
        if (CollectionUtils.isEmpty(contextFactoryItemList))
            return ret + "<empty>" + "]";
        FactoryItem factoryItem;
        for (int i = 1; i <= contextFactoryItemList.size(); i++) {
            factoryItem = contextFactoryItemList.get(i-1);
            ret
                    .append(System.lineSeparator())
                    .append(StringUtils.tabs(currentDepth))
                    .append(i)
                    .append(" -> ")
                    .append(factoryItem.toString(currentDepth + 1, limit, visited));
        }
        return ret + "]";
    }


//    public enum EraseMarker {
//        ERASE,
//        DO_NOT_ERASE,
//        CURRENT_ITEM_SKIPPED,
//        GLOBALLY_LOCKED
//    }
}
