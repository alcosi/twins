package org.twins.core.domain.twinoperation;

import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;

@Getter
public class TwinDelete extends TwinOperation {
    boolean globalLock = false;

    public TwinDelete(TwinEntity twin, boolean globalLock) {
        this.twinEntity = twin;
        this.globalLock = globalLock;
    }
}
