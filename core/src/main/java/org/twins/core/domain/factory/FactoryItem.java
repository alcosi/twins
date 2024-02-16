package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggableImpl;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FactoryItem extends EasyLoggableImpl {
    private FactoryContext factoryContext;
    private TwinOperation output;
    private List<FactoryItem> contextFactoryItemList;

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
        switch (level) {
            case SHORT:
                return "factoryItem[" + System.identityHashCode(this) + "]";
            case NORMAL:
                if (output != null)
                    details = output instanceof TwinCreate ? "createTwin" : "updateTwin";
                return "factoryItem[" + System.identityHashCode(this) + "] " + details;
            default:
                String ret = "factoryItem[" + System.identityHashCode(this) + "]: ";
                if (output != null) {
                    ret += output instanceof TwinCreate ? "createTwin" : "updateTwin";
                    if (output.getTwinEntity() != null) {
                        ret += "[class:" + output.getTwinEntity().getTwinClassId();
                        ret += output instanceof TwinUpdate ? ", id:" + output.getTwinEntity().getId() : "";
                        ret += "]";
                    }
                }
                return ret;
        }
    }

    @Override
    public String toString() {
        return "FactoryItem";
    }
}
