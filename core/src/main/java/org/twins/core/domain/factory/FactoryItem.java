package org.twins.core.domain.factory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggableImpl;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinOperation;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FactoryItem extends EasyLoggableImpl {
    private FactoryContext factoryContext;
    private TwinOperation outputTwin;
    private List<TwinEntity> contextTwinList;

    public TwinEntity getContextFirstTwin() {
        if (CollectionUtils.isEmpty(contextTwinList))
            return null;
        return contextTwinList.get(0);
    }

    @Override
    public String easyLog(Level level) {
        String details = "";
        switch (level) {
            case SHORT:
                return "factoryItem[" + System.identityHashCode(this) + "]";
            case NORMAL:
                if (outputTwin != null)
                    details = outputTwin instanceof TwinCreate ? "createTwin" : "updateTwin";
                return "factoryItem[" + System.identityHashCode(this) + "] " + details;
            default:
                if (outputTwin != null) {
                    details = outputTwin instanceof TwinCreate ? "createTwin" : "updateTwin";
                    if (outputTwin.getTwinEntity() != null) {
                        details += outputTwin instanceof TwinCreate ? "[class:" + outputTwin.getTwinEntity().getTwinClassId() + "]" : "[id:" + outputTwin.getTwinEntity().getId() + "]";
                    }
                }
                return "factoryItem[" + System.identityHashCode(this) + "] " + details;
        }
    }
}
