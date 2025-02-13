package org.cambium.common.kit;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.MapUtils;

import java.util.*;
import java.util.function.Function;

public class KitGroupedObj<E, K, GK, GE> extends KitGrouped<E, K, GK> {
    protected Map<GK, GE> groupingObjectMap;
    private final Function<? super E, ? extends GE> functionGetGroupingObject;
    public KitGroupedObj(Collection<E> collection, Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, Function<? super E, ? extends GE> functionGetGroupingObject) {
        super(collection, functionGetId, functionGetGroupingId);
        this.functionGetGroupingObject = functionGetGroupingObject;
    }

    public KitGroupedObj(Function<? super E, ? extends K> functionGetId, Function<? super E, ? extends GK> functionGetGroupingId, Function<? super E, ? extends GE> functionGetGroupingObject) {
        super(functionGetId, functionGetGroupingId);
        this.functionGetGroupingObject = functionGetGroupingObject;
    }

    @Override
    public boolean add(E e) {
        groupingObjectMap = null; //invalidate
        return super.add(e);
    }

    public Map<GK, GE> getGroupingObjectMap() {
        if (groupingObjectMap != null)
            return groupingObjectMap;
        if (isEmpty() || functionGetGroupingObject == null)
            return Collections.EMPTY_MAP;
        getGroupedMap();
        if (groupedMap == null) // looks like this is too much
            return Collections.EMPTY_MAP;
        groupingObjectMap = new HashMap<>();
        GK groupingId;
        GE groupingObject;
        for (Map.Entry<GK, List<E>> entry : groupedMap.entrySet()) {
            groupingId = entry.getKey();
            if (CollectionUtils.isEmpty(entry.getValue())) // this is not possible, but let's do extra check
                continue;
            groupingObject = functionGetGroupingObject.apply(entry.getValue().get(0));
            groupingObjectMap.put(groupingId, groupingObject);
        }
        return groupingObjectMap;
    }

    public List<ImmutablePair<GE, List<E>>> getGroupedList() {
        //todo cache result in variable
        var ret = new ArrayList<ImmutablePair<GE, List<E>>>();
        for (var entry : getGroupedMap().entrySet()) {
            ret.add(new ImmutablePair<>(getGroupingObject(entry.getKey()), entry.getValue()));
        }
        return ret;
    }

    public GE getGroupingObject(GK key) {
        getGroupingObjectMap();
        if (MapUtils.isEmpty(groupingObjectMap))
            return null;
        return groupingObjectMap.get(key);
    }
}
