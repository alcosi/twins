package org.twins.core.domain;

import org.cambium.common.PublicCloneable;

import java.util.ArrayList;
import java.util.List;

public final class EntityCUDHelper {

    public static <T extends PublicCloneable<T>> List<T> getOrInitCreateList(EntityCUD<T> cud) {
        if (cud.getCreateList() == null) {
            cud.setCreateList(new ArrayList<>());
        }
        return cud.getCreateList();
    }

    public static <T extends PublicCloneable<T>> List<T> getOrInitUpdateList(EntityCUD<T> cud) {
        if (cud.getUpdateList() == null) {
            cud.setUpdateList(new ArrayList<>());
        }
        return cud.getUpdateList();
    }

    public static <T extends PublicCloneable<T>> List<T> getOrInitDeleteList(EntityCUD<T> cud) {
        if (cud.getDeleteList() == null) {
            cud.setDeleteList(new ArrayList<>());
        }
        return cud.getDeleteList();
    }
}
