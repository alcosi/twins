package org.twins.core.domain.twinoperation;

import java.util.ArrayList;

public class TwinCreateStage extends ArrayList<TwinCreate> {
    public TwinCreateStage(int initSize) {
        super(initSize);
    }

    public static TwinCreateStage of(TwinCreate twinCreate) {
        var ret =  new TwinCreateStage(1);
        ret.add(twinCreate);
        return ret;
    }
}
