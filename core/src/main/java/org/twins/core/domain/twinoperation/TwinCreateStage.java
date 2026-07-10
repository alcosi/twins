package org.twins.core.domain.twinoperation;

import lombok.SneakyThrows;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.UuidUtils;
import org.jetbrains.annotations.NotNull;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.*;

public class TwinCreateStage implements Iterable<TwinCreate> {
    private Map<UUID, TwinCreate> map;
    private Kit<TwinEntity, UUID> entities;
    private KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> entitiesGroupedByClass;
    boolean frozen = false;

    public TwinCreateStage(int initSize) {
        map = new LinkedHashMap<>(initSize);
        entities = new Kit<>(TwinEntity::getId);
    }

    @SneakyThrows
    public static TwinCreateStage of(TwinCreate twinCreate) {
        var ret = new TwinCreateStage(1);
        ret.add(twinCreate);
        return ret;
    }

    public TwinCreateStage add(TwinCreate twinCreate) throws ServiceException {
        if (frozen)
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "TwinCreate stage is already freezed");
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        if (entities == null) {
            entities = new Kit<>(TwinEntity::getId);
        }
        var entity = twinCreate.getTwinEntity();
        if (entity.getId() == null) {
            entity.setId(UuidUtils.generate());
        }
        map.put(entity.getId(), twinCreate);
        entities.add(entity);
        return this;
    }

    public Collection<TwinCreate> getTwinCreates() {
        return map != null ? map.values() : Collections.emptyList();
    }

    public Kit<TwinEntity, UUID> getEntitiesKit() {
        return entities != null ? entities : Kit.emptyKit();
    }

    public Collection<TwinEntity> getEntities() {
        return entities != null ? entities.getCollection() : Collections.emptyList();
    }

    public KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> getEntitiesGroupedByClass() {
        return entitiesGroupedByClass != null ? entitiesGroupedByClass : KitGroupedObj.emptyKitGroupedObj();
    }

    @NotNull
    @Override
    public Iterator<TwinCreate> iterator() {
        if (map == null) {
            return Collections.emptyIterator();
        }
        return frozen
                ? Collections.unmodifiableCollection(map.values()).iterator()
                : map.values().iterator();
    }

    public void freeze() {
        frozen = true;
        entities.freeze();
        entitiesGroupedByClass = new KitGroupedObj<>(entities.getCollection(), TwinEntity::getId, TwinEntity::getTwinClassId, TwinEntity::getTwinClass);
    }

    public TwinCreate getTwinCreate(UUID twinEntityId) {
        return map != null ? map.get(twinEntityId) : null;
    }

    public long size() {
        return map != null ? map.size() : 0;
    }
}
