package org.twins.core.domain.twinoperation;

import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;

@Getter
public class TwinDelete extends TwinOperation {
    boolean causeGlobalLock;
    private String eraseDetails;

    public TwinDelete(TwinEntity twin, boolean causeGlobalLock, String eraseDetails) {
        this.eraseDetails = eraseDetails;
        this.twinEntity = twin;
        this.causeGlobalLock = causeGlobalLock;
    }
}
