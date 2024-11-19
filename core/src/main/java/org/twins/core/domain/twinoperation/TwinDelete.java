package org.twins.core.domain.twinoperation;

import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.EraseAction;

@Getter
public class TwinDelete extends TwinOperation {
    EraseAction eraseAction;

    public TwinDelete(TwinEntity twin, EraseAction eraseAction) {
        this.twinEntity = twin;
        this.eraseAction = eraseAction;
    }
}
