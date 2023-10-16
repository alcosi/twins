package org.twins.core.mappers.rest;


import java.util.*;

public abstract class RestListDTOMapper<T, S> implements RestDTOMapper<T, S> {
    public List<S> convertList(List<T> srcList, MapperProperties mapperProperties) throws Exception {
        List<S> ret = new ArrayList<>();
        for (T src : srcList) {
            ret.add(this.convert(src, mapperProperties));
        }
        return ret;
    }

    public List<S> convertList(List<T> srcList) throws Exception {
        if (srcList == null)
            return null;
        return convertList(srcList, new MapperProperties());
    }

    public Map<UUID, S> convertMap(Map<UUID, T> srcMap, MapperProperties mapperProperties) throws Exception {
        Map<UUID, S> ret = new LinkedHashMap<>();
        for (Map.Entry<UUID, T>  src : srcMap.entrySet()) {
            ret.put(src.getKey(), this.convert(src.getValue(), mapperProperties));
        }
        return ret;
    }
}
